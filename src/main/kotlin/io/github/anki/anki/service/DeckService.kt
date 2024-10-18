package io.github.anki.anki.service

import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.DeckRepository
import io.github.anki.anki.repository.mongodb.document.DocumentStatus
import io.github.anki.anki.repository.mongodb.document.MongoDeck
import io.github.anki.anki.service.exceptions.CardDoesNotExistException
import io.github.anki.anki.service.exceptions.DeckDoesNotExistException
import io.github.anki.anki.service.model.Deck
import io.github.anki.anki.service.model.mapper.toDeck
import io.github.anki.anki.service.model.mapper.toMongo
import io.github.anki.anki.service.utils.toObjectId
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class DeckService(
    private val deckRepository: DeckRepository,
    private val cardRepository: CardRepository,
) {
    fun createNewDeck(deck: Deck): Mono<Deck> =
        deckRepository
            .insert(deck.toMongo())
            .map { mongoDeck -> mongoDeck.toDeck() }

    fun getDecks(userId: String): Flux<Deck> =
        deckRepository
            .findByUserIdWithStatus(userId.toObjectId())
            .map(MongoDeck::toDeck)

    fun updateDeck(deck: Deck): Mono<Deck> {
        val deckId = deck.id ?: throw IllegalArgumentException("Deck id can not be null")
        return validateUserHasPermissions(deckId, deck.userId)
            .then(getDeckById(deckId))
            .flatMap { mongoDeck -> saveIfNotEquals(mongoDeck, deck) }
    }

    fun deleteDeck(deckId: String, userId: String): Mono<Void> =
        deckRepository.softDelete(deckId.toObjectId())
            .mergeWith(cardRepository.softDeleteByDeckId(deckId.toObjectId()))
            .startWith(validateUserHasPermissions(deckId, userId))
            .then()

    fun validateUserHasPermissions(deckId: String, userId: String): Mono<Void> =
        hasPermissions(deckId, userId)
            .filter { result -> result }
            .switchIfEmpty(Mono.error(DeckDoesNotExistException.fromDeckIdAndUserId(deckId, userId)))
            .then()

    private fun getDeckById(deckId: String): Mono<MongoDeck> =
        deckRepository.findByIdWithStatus(deckId.toObjectId(), DocumentStatus.ACTIVE)
            .switchIfEmpty(Mono.error(DeckDoesNotExistException.fromDeckId(deckId)))

    private fun hasPermissions(deckId: String, userId: String): Mono<Boolean> =
        deckRepository.existsByIdAndUserIdWithStatus(
            id = deckId.toObjectId(),
            userId = userId.toObjectId(),
            status = DocumentStatus.ACTIVE,
        )

    private fun saveIfNotEquals(mongoDeck: MongoDeck, deck: Deck): Mono<Deck> {
        val updatedMongoDeck = mongoDeck.update(deck)
        if (mongoDeck == updatedMongoDeck) {
            return Mono.just(mongoDeck.toDeck())
        }
        return deckRepository
            .save(updatedMongoDeck)
            .map(MongoDeck::toDeck)
    }

    private fun MongoDeck.update(deck: Deck): MongoDeck =
        this.copy(
            name = deck.name ?: this.name,
            description = deck.description ?: this.description,
        )
}
