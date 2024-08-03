package io.github.anki.anki.service

import io.github.anki.anki.controller.exceptions.CardDoesNotExistException
import io.github.anki.anki.controller.exceptions.DeckDoesNotExistException
import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.document.MongoCard
import io.github.anki.anki.service.model.Card
import io.github.anki.anki.service.model.mapper.toCard
import io.github.anki.anki.service.model.mapper.toMongo
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class CardsService(
    private val cardRepository: CardRepository,
) {

    fun createNewCard(card: Card): Card {
        LOG.info("Creating new card: {}", card)
        return cardRepository.insert(
            card.toMongo()
        )
            .toCard()
            .also { LOG.info("Successfully saved new card: {}", it) }
    }

    fun getAllCardsFromDeck(deckId: String): List<Card> =
        cardRepository.findByDeckId(ObjectId(deckId)).map { it.toCard() }

    fun updateCard(card: Card): Card {
        val mongoCard = getCardById(card.id!!)
        return cardRepository.save(getUpdatedMongoCard(mongoCard, card)).toCard()
    }

    fun deleteCard(cardId: String) {
        LOG.info("Deleting card with id: {}", cardId)
        cardRepository.deleteById(cardId)
        LOG.info("Successfully deleted card with id: {}", cardId)
    }

    private fun getCardById(cardId: String): MongoCard =
        cardRepository.findByIdOrNull(ObjectId(cardId))
            ?: throw CardDoesNotExistException()

    private fun getUpdatedMongoCard(mongoCard: MongoCard, card: Card): MongoCard =
        mongoCard.copy().apply {
            card.cardKey?.takeIf { it != cardKey }?.let { cardKey = it }
            card.cardValue?.takeIf { it != cardValue }?.let { cardValue = it }
        }

    companion object {
        private val LOG = LoggerFactory.getLogger(CardsService::class.java)
    }
}
