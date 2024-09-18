package io.github.anki.anki.service

import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.DeckRepository
import io.github.anki.anki.repository.mongodb.document.DocumentStatus
import io.github.anki.anki.repository.mongodb.document.MongoDeck
import io.github.anki.anki.service.exceptions.DeckDoesNotExistException
import io.github.anki.anki.service.model.Deck
import io.github.anki.anki.service.model.mapper.toDeck
import io.github.anki.anki.service.model.mapper.toMongo
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

@Service
class DeckService(
    private val deckRepository: DeckRepository,
    private val cardRepository: CardRepository,
) {
    fun createNewDeck(deck: Deck): Deck {
        return deckRepository
            .insert(deck.toMongo())
            .get()
            .toDeck()
    }

    fun getDecks(userId: String): List<Deck> {
        return deckRepository
            .findByUserIdWithStatus(ObjectId(userId))
            .get()
            .map { it.toDeck() }
    }

    fun updateDeck(deck: Deck): Deck {
        val deckId = deck.id ?: throw IllegalArgumentException("Deck id can not be null")
        validateUserHasPermissions(deckId, deck.userId)
        val mongoDeck =
            deckRepository.findByIdAndUserIdWithStatus(
                id = ObjectId(deckId),
                userId = ObjectId(deck.userId),
                status = DocumentStatus.ACTIVE,
            ).get() ?: throw DeckDoesNotExistException.fromDeckIdAndUserId(deckId, deck.userId)
        val updatedMongoDeck = mongoDeck.update(deck)
        if (mongoDeck == updatedMongoDeck) {
            return mongoDeck.toDeck()
        }
        return deckRepository
            .save(updatedMongoDeck)
            .get()
            .toDeck()
    }

    fun deleteDeck(deckId: String, userId: String) {
        validateUserHasPermissions(deckId, userId)
        CompletableFuture.allOf(
            deckRepository.softDelete(ObjectId(deckId)),
            cardRepository.softDeleteByDeckId(ObjectId(deckId)),
        ).join()
    }

    fun validateUserHasPermissions(deckId: String, userId: String) {
        if (!hasPermissions(deckId, userId)) {
            throw DeckDoesNotExistException.fromDeckIdAndUserId(deckId, userId)
        }
    }

    private fun hasPermissions(deckId: String, userId: String): Boolean =
        deckRepository.existsByIdAndUserIdWithStatus(
            id = ObjectId(deckId),
            userId = ObjectId(userId),
            status = DocumentStatus.ACTIVE,
        ).get()

    private fun MongoDeck.update(deck: Deck): MongoDeck =
        this.copy(
            name = deck.name ?: this.name,
            description = deck.description ?: this.description,
        )
}
