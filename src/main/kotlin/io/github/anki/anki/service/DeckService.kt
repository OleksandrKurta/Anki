package io.github.anki.anki.service

import io.github.anki.anki.controller.DeckDoesNotExistException
import io.github.anki.anki.repository.mongodb.DeckRepository
import io.github.anki.anki.repository.mongodb.document.MongoDeck
import io.github.anki.anki.service.model.Deck
import io.github.anki.anki.service.model.mapper.toDeck
import io.github.anki.anki.service.model.mapper.toMongo
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service


@Service
class DeckService(
    private val deckRepository: DeckRepository,
) {
    fun createNewDeck(deck: Deck): Deck {
        LOG.info("Creating new deck: {}", deck)
        return deckRepository.insert(
            deck.toMongo()
        )
            .toDeck()
            .also { LOG.info("Successfully saved new collection: {}", it) }
    }

    fun getDecks(userId: String): List<Deck> {
        return deckRepository
            .also { LOG.info("Getting decks by userId {}", userId) }
            .run { findByUserId(ObjectId(userId)) }
            .map { it.toDeck() }
            .also { LOG.info("Got {} decks", it.size) }
    }

    fun updateDeck(deck: Deck): Deck {
        val mongoDeck = deckRepository.findByIdAndUserId(
            id = ObjectId(deck.id!!),
            userId = ObjectId(deck.userId),
        )
        if ( mongoDeck == null) {
            throw DeckDoesNotExistException()
        } else {
            return deckRepository.save(getUpdatedMongoDeck(mongoDeck, deck)).toDeck()
        }
    }

    fun deleteCard(deckId: String) {
        LOG.info("Deleting deck with id: {}", deckId)
        deckRepository.deleteById(deckId)
        LOG.info("Successfully deleted card with id: {}", deckId)
    }

    private fun getUpdatedMongoDeck(mongoDeck: MongoDeck, deck: Deck): MongoDeck =
        mongoDeck.copy().apply {
                deck.name?.takeIf { it != name }?.let { name = it }
                deck.description?.takeIf { it != description }?.let { description = it }
            }

    companion object {
        private val LOG = LoggerFactory.getLogger(DeckService::class.java)
    }
}
