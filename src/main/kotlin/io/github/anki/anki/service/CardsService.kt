package io.github.anki.anki.service

import io.github.anki.anki.controller.exceptions.CardDoesNotExistException
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
    private val deckService: DeckService,
) {
    fun createNewCard(deckId: String, userId: String, card: Card): Card {
        deckService.getDeckByIdAndUserId(deckId, userId)
        LOG.info("Creating new card: {}", card)
        return cardRepository.insert(
            card.toMongo(),
        )
            .toCard()
            .also { LOG.info("Successfully saved new card: {}", it) }
    }

    fun getAllCardsFromDeck(deckId: String, userId: String): List<Card> =
        deckService.getDeckByIdAndUserId(deckId, userId)
            .run { cardRepository.findByDeckId(ObjectId(deckId)).map { it.toCard() } }

    fun updateCard(deckId: String, userId: String, card: Card): Card =
        deckService.getDeckByIdAndUserId(deckId, userId)
            .run {
                cardRepository
                    .save(getUpdatedMongoCard(getCardById(card.id!!), card))
                    .toCard()
            }

    fun deleteCard(deckId: String, userId: String, cardId: String) {
        deckService.getDeckByIdAndUserId(deckId, userId)
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
