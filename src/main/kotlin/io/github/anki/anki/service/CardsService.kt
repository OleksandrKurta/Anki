package io.github.anki.anki.service

import io.github.anki.anki.api.nats.nats.NatsSubject
import io.github.anki.anki.api.nats.v1.card.commands.DeleteCardRequest
import io.github.anki.anki.api.nats.v1.card.commands.DeleteCardResponse
import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.document.DocumentStatus
import io.github.anki.anki.repository.mongodb.document.MongoCard
import io.github.anki.anki.service.exceptions.CardDoesNotExistException
import io.github.anki.anki.service.model.Card
import io.github.anki.anki.service.model.Pagination
import io.github.anki.anki.service.model.mapper.toCard
import io.github.anki.anki.service.model.mapper.toMongo
import io.github.anki.anki.service.utils.toObjectId
import io.nats.client.Connection
import io.nats.client.Message
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono

@Service
class CardsService(
    private val cardRepository: CardRepository,
    private val deckService: DeckService,
    private val natsClient: Connection,
) {
    fun createNewCard(userId: String, card: Card): Mono<Card> =
        deckService.validateUserHasPermissions(card.deckId, userId)
            .flatMap { cardRepository.insert(card.toMongo()) }
            .map { it.toCard() }

    fun findCardsByDeckWithPagination(deckId: String, userId: String, pagination: Pagination): Flux<Card> =
        deckService.validateUserHasPermissions(deckId, userId)
            .flatMapMany {
                cardRepository
                    .findByDeckIdWithStatus(
                        deckId = deckId.toObjectId(),
                        limit = pagination.limit,
                        offset = pagination.offset,
                    )
            }
            .map { it.toCard() }

    fun updateCard(userId: String, card: Card): Mono<Card> =
        Mono.just(card)
            .mapNotNull { it.id }
            .switchIfEmpty { Mono.error(IllegalArgumentException("Card Id can not be null")) }
            .flatMap { deckService.validateUserHasPermissions(card.deckId, userId) }
            .flatMap { getCardById(card.id!!) }
            .flatMap { saveIfNotEquals(it, card) }

    fun deleteCard(deckId: String, userId: String, cardId: String): Mono<Unit> =
        deckService.validateUserHasPermissions(deckId, userId)
            .flatMap { requestDeleteCard(cardId) }
            .flatMap { validateDeleteCardResponse(it) }

    private fun saveIfNotEquals(mongoCard: MongoCard, card: Card): Mono<Card> {
        val updatedMongoCard = mongoCard.update(card)
        return if (mongoCard == updatedMongoCard) {
            LOG.info("Nothing to change in Card with id {}", mongoCard.id)
            Mono.just(mongoCard.toCard())
        } else {
            cardRepository
                .save(updatedMongoCard)
                .map { it.toCard() }
        }
    }

    private fun requestDeleteCard(cardId: String): Mono<Message> =
        natsClient.request(
            NatsSubject.Card.CARD_DELETE_COMMAND,
            DeleteCardRequest.newBuilder().setCardId(cardId).build().toByteArray(),
        )
            .toMono()

    private fun validateDeleteCardResponse(msg: Message): Mono<Unit> {
        val response = DeleteCardResponse.parseFrom(msg.data)
        if (response.hasSuccess()) return Mono.empty()
        return Mono.error(RuntimeException(response.failure.reason))
    }

    private fun getCardById(cardId: String): Mono<MongoCard> =
        cardRepository.findByIdWithStatus(cardId.toObjectId(), DocumentStatus.ACTIVE)
            .switchIfEmpty { Mono.error(CardDoesNotExistException.fromCardId(cardId)) }

    private fun MongoCard.update(card: Card): MongoCard =
        this.copy(
            key = card.key ?: this.key,
            value = card.value ?: this.value,
        )

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(CardsService::class.java)
    }
}
