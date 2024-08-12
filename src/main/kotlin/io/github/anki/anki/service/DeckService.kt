package io.github.anki.anki.service

import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.DeckRepository
import io.github.anki.anki.repository.mongodb.document.MongoDeck
import io.github.anki.anki.service.exceptions.DeckDoesNotExistException
import io.github.anki.anki.service.model.Deck
import io.github.anki.anki.service.model.mapper.toDeck
import io.github.anki.anki.service.model.mapper.toMongo
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class DeckService(
    private val deckRepository: DeckRepository,
    private val cardRepository: CardRepository,
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
        val mongoDeck = getDeckByIdAndUserId(deck.id!!, deck.userId)
        val updatedMongoDeck = mongoDeck.update(deck)
        if (mongoDeck == updatedMongoDeck) {
            return mongoDeck.toDeck()
        }
        return deckRepository
            .save(updatedMongoDeck)
            .toDeck()
    }

    fun deleteDeck(deckId: String) {
        deckRepository.softDelete(ObjectId(deckId))
        cardRepository.softDeleteByDeckId(ObjectId(deckId))
    }

    fun getDeckByIdAndUserId(deckId: String, userId: String): MongoDeck {
        val mongoDeck = deckRepository.findByIdAndUserIdWithStatus(id = ObjectId(deckId), userId = ObjectId(userId))
        if (mongoDeck != null) {
            return mongoDeck
        }
        throw DeckDoesNotExistException.fromDeckIdAndUserId(deckId, userId)
    }

    private fun MongoDeck.update(deck: Deck): MongoDeck =
        this.copy(
            name = deck.name ?: this.name,
            description = deck.description ?: this.description,
        )
}
