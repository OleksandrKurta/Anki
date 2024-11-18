package io.github.anki.anki.service

import io.github.anki.anki.api.kafka.v1.deck.DeckEvent
import io.github.anki.anki.api.kafka.v1.deck.DeckEventType
import io.github.anki.anki.api.nats.kafka.KafkaTopic
import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.DeckRepository
import io.github.anki.anki.repository.mongodb.document.DocumentStatus
import io.github.anki.anki.repository.mongodb.document.MongoDeck
import io.github.anki.anki.service.exceptions.DeckDoesNotExistException
import io.github.anki.anki.service.model.Deck
import io.github.anki.anki.service.model.mapper.toDeck
import io.github.anki.anki.service.model.mapper.toMongo
import io.github.anki.anki.service.utils.toObjectId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono

@Service
class DeckService(
    private val deckRepository: DeckRepository,
    private val cardRepository: CardRepository,
    private val kafkaProducer: ReactiveKafkaProducerTemplate<String, DeckEvent>,
) {
    fun createNewDeck(deck: Deck): Mono<Deck> =
        deckRepository
            .insert(deck.toMongo())
            .flatMap { sendEventToKafka(it.id!!.toString(), DeckEventType.CREATED).then(it.toMono()) }
            .map { mongoDeck -> mongoDeck.toDeck() }

    fun getDecks(userId: String): Flux<Deck> =
        deckRepository
            .findByUserIdWithStatus(userId.toObjectId())
            .map { it.toDeck() }

    fun updateDeck(deck: Deck): Mono<Deck> =
        Mono.just(deck)
            .mapNotNull { deck.id }
            .switchIfEmpty { Mono.error(IllegalArgumentException("Deck id can not be null")) }
            .flatMap { validateUserHasPermissions(deck.id!!, deck.userId) }
            .flatMap { getDeckById(deck.id!!) }
            .flatMap { mongoDeck -> saveIfNotEquals(mongoDeck, deck) }
            .flatMap { sendEventToKafka(deck.id!!, DeckEventType.UPDATED).then(it.toMono()) }

    fun deleteDeck(deckId: String, userId: String): Mono<Unit> =
        validateUserHasPermissions(deckId, userId)
            .flatMapMany {
                Flux.zip(
                    deckRepository.softDelete(deckId.toObjectId()),
                    cardRepository.softDeleteByDeckId(deckId.toObjectId()),
                )
            }
            .then(Mono.defer { sendEventToKafka(deckId, DeckEventType.DELETED) })

    fun validateUserHasPermissions(deckId: String, userId: String): Mono<Boolean> =
        hasPermissions(deckId, userId)
            .filter { it }
            .switchIfEmpty { Mono.error(DeckDoesNotExistException.fromDeckIdAndUserId(deckId, userId)) }
            .doOnSuccess { LOG.info("User {} has permissions to deck {}", userId, deckId) }

    private fun getDeckById(deckId: String): Mono<MongoDeck> =
        deckRepository.findByIdWithStatus(deckId.toObjectId(), DocumentStatus.ACTIVE)
            .switchIfEmpty { Mono.error(DeckDoesNotExistException.fromDeckId(deckId)) }

    private fun hasPermissions(deckId: String, userId: String): Mono<Boolean> =
        deckRepository.existsByIdAndUserIdWithStatus(
            id = deckId.toObjectId(),
            userId = userId.toObjectId(),
            status = DocumentStatus.ACTIVE,
        )

    private fun saveIfNotEquals(mongoDeck: MongoDeck, deck: Deck): Mono<Deck> {
        val updatedMongoDeck = mongoDeck.update(deck)
        return if (mongoDeck == updatedMongoDeck) {
            LOG.info("Nothing to change in Deck with id {}", mongoDeck.id)
            Mono.just(mongoDeck.toDeck())
        } else {
            deckRepository
                .save(updatedMongoDeck)
                .map { it.toDeck() }
        }
    }

    private fun sendEventToKafka(deckId: String, deckEventType: DeckEventType): Mono<Unit> {
        val deckEvent: DeckEvent =
            DeckEvent.newBuilder()
                .setDeckId(deckId)
                .setDeckEventType(deckEventType)
                .build()
        return kafkaProducer.send(KafkaTopic.Deck.EVENT, deckEvent)
            .doOnNext { LOG.info("Sending message to kafka: message={}", deckEvent.toString()) }
            .then(Mono.empty())
    }

    private fun MongoDeck.update(deck: Deck): MongoDeck =
        this.copy(
            name = deck.name ?: this.name,
            description = deck.description ?: this.description,
        )

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(DeckService::class.java)
    }
}
