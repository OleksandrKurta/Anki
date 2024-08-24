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
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Service
import java.util.concurrent.Future

@Service
class DeckService(
    private val deckRepository: DeckRepository,
    private val cardRepository: CardRepository,
    private var threadPool: ThreadPoolTaskExecutor,
) {
    fun createNewDeck(deck: Deck): Deck {
        return deckRepository
            .insert(deck.toMongo())
            .toDeck()
    }

    fun getDecks(userId: String): List<Deck> {
        return deckRepository
            .findByUserIdWithStatus(ObjectId(userId))
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
            ) ?: throw DeckDoesNotExistException.fromDeckIdAndUserId(deckId, deck.userId)
        val updatedMongoDeck = mongoDeck.update(deck)
        if (mongoDeck == updatedMongoDeck) {
            return mongoDeck.toDeck()
        }
        return deckRepository
            .save(updatedMongoDeck)
            .toDeck()
    }

    fun deleteDeck(deckId: String, userId: String) {
        validateUserHasPermissions(deckId, userId)
        val deleteDeckFuture: Future<*> = threadPool.submit { deckRepository.softDelete(ObjectId(deckId)) }
        val deleteCardsFuture: Future<*> = threadPool.submit { cardRepository.softDeleteByDeckId(ObjectId(deckId)) }
        deleteDeckFuture.get()
        deleteCardsFuture.get()
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
        )

    private fun MongoDeck.update(deck: Deck): MongoDeck =
        this.copy(
            name = deck.name ?: this.name,
            description = deck.description ?: this.description,
        )
}
