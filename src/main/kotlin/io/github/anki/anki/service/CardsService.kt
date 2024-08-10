package io.github.anki.anki.service

import io.github.anki.anki.controller.exceptions.CardDoesNotExistException
import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.document.MongoCard
import io.github.anki.anki.service.model.Card
import io.github.anki.anki.service.model.mapper.toCard
import io.github.anki.anki.service.model.mapper.toMongo
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CardsService(
    private val cardRepository: CardRepository,
    private val deckService: DeckService,
) {
    fun createNewCard(deckId: String, userId: String, card: Card): Card {
        deckService.getDeckByIdAndUserId(deckId, userId)
        LOG.info("Creating new card: {}", card)
        return cardRepository.insert(card.toMongo())
            .toCard()
            .also { LOG.info("Successfully saved new card: {}", it) }
    }

    fun getAllCardsFromDeck(deckId: String, userId: String): List<Card> =
        deckService.getDeckByIdAndUserId(deckId, userId)
            .run { cardRepository.findByDeckId(ObjectId(deckId)).map { it.toCard() } }

    fun updateCard(deckId: String, userId: String, card: Card): Card {
        deckService.getDeckByIdAndUserId(deckId, userId)
        val mongoCard = getCardById(card.id!!)
        val updatedMongoCard = mongoCard.update(card)
        if (mongoCard == updatedMongoCard) {
            return mongoCard.toCard()
        }
        return cardRepository.save(updatedMongoCard).toCard()
    }

    fun deleteCard(deckId: String, userId: String, cardId: String) {
        deckService.getDeckByIdAndUserId(deckId, userId)
        LOG.info("Deleting card with id: {}", cardId)
        cardRepository.deleteById(ObjectId(cardId))
        LOG.info("Successfully deleted card with id: {}", cardId)
    }

    private fun getCardById(cardId: String): MongoCard =
        cardRepository.findById(ObjectId(cardId))
            ?: throw CardDoesNotExistException()

    private fun MongoCard.update(card: Card): MongoCard =
        this.copy(
            cardKey = card.cardKey ?: this.cardKey,
            cardValue = card.cardValue ?: this.cardValue,
        )

    companion object {
        private val LOG = LoggerFactory.getLogger(CardsService::class.java)
    }
}
