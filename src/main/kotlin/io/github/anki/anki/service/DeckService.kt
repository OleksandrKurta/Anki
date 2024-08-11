package io.github.anki.anki.service

import io.github.anki.anki.controller.exceptions.DeckDoesNotExistException
import io.github.anki.anki.repository.mongodb.CardRepository
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
    private val cardRepository: CardRepository,
) {
    fun createNewDeck(deck: Deck): Deck {
        LOG.info("Creating new deck: {}", deck)
        return deckRepository.insert(
            deck.toMongo(),
        )
            .toDeck()
            .also { LOG.info("Successfully saved new Deck: {}", it) }
    }

    fun getDecks(userId: String): List<Deck> {
        LOG.info("Getting decks by userId {}", userId)
        return deckRepository
            .findByUserId(ObjectId(userId))
            .map { it.toDeck() }
            .also { LOG.info("Got {} decks", it.size) }
    }

    fun updateDeck(deck: Deck): Deck {
        val mongoDeck = getDeckByIdAndUserId(deck.id!!, deck.userId)
        val updatedMongoDeck = mongoDeck.update(deck)
        if (mongoDeck == updatedMongoDeck) {
            LOG.info("No changes. Nothing to update")
            return mongoDeck.toDeck()
        }
        LOG.info("Save updated MongoDeck {}", updatedMongoDeck)
        return deckRepository.save(updatedMongoDeck)
            .also { LOG.info("Updated MongoDeck is saved {}", it) }
            .toDeck()
    }

    fun deleteDeck(deckId: String) {
        LOG.info("Deleting deck with id: {}", deckId)
        deckRepository.deleteById(ObjectId(deckId))
        LOG.info("Successfully deleted deck with id: {}", deckId)
        cardRepository.deleteByDeckId(ObjectId(deckId))
        LOG.info("Successfully deleted all cards with deckId: {}", deckId)
    }

    fun getDeckByIdAndUserId(deckId: String, userId: String): MongoDeck {
        LOG.info("Getting MongoDeck from DB by deckId = {}, userId = {}", deckId, userId)
        val mongoDeck = deckRepository.findByIdAndUserId(id = ObjectId(deckId), userId = ObjectId(userId))
        if (mongoDeck != null) {
            LOG.info("Got {}", mongoDeck)
            return mongoDeck
        }
        LOG.info("MongoDeck was not found with given deckId and userId")
        throw DeckDoesNotExistException()
    }

    private fun MongoDeck.update(deck: Deck): MongoDeck =
        this.copy(
            name = deck.name ?: this.name,
            description = deck.description ?: this.description,
        )

    companion object {
        private val LOG = LoggerFactory.getLogger(DeckService::class.java)
    }
}
