package io.github.anki.anki.service

import io.github.anki.anki.repository.mongodb.DeckRepository
import io.github.anki.anki.service.model.Deck
import io.github.anki.anki.service.model.mapper.toDeck
import io.github.anki.anki.service.model.mapper.toMongo
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

    companion object {
        private val LOG = LoggerFactory.getLogger(CardsService::class.java)
    }
}
