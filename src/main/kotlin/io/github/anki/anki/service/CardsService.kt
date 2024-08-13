package io.github.anki.anki.service

import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.document.MongoCard
import io.github.anki.anki.service.exceptions.CardDoesNotExistException
import io.github.anki.anki.service.model.Card
import io.github.anki.anki.service.model.mapper.toCard
import io.github.anki.anki.service.model.mapper.toMongo
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class CardsService(
    private val cardRepository: CardRepository,
    private val deckService: DeckService,
) {
    fun createNewCard(userId: String, card: Card): Card {
        deckService.getDeckByIdAndUserId(card.deckId, userId)
        return cardRepository
            .insert(card.toMongo())
            .toCard()
    }

    fun findCardsByDeck(deckId: String, userId: String): List<Card> {
        deckService.getDeckByIdAndUserId(deckId, userId)
        return cardRepository
            .findByDeckIdWithStatus(ObjectId(deckId))
            .map { it.toCard() }
    }

    fun updateCard(userId: String, card: Card): Card {
        deckService.getDeckByIdAndUserId(card.deckId, userId)
        val mongoCard = getCardById(card.id ?: throw IllegalArgumentException("Card Id can not be null"))
        val updatedMongoCard = mongoCard.update(card)
        if (mongoCard == updatedMongoCard) {
            return mongoCard.toCard()
        }
        return cardRepository.save(updatedMongoCard).toCard()
    }

    fun deleteCard(deckId: String, userId: String, cardId: String) {
        deckService.getDeckByIdAndUserId(deckId, userId)
        cardRepository.softDelete(ObjectId(cardId))
    }

    private fun getCardById(cardId: String): MongoCard {
        val mongoCard = cardRepository.findById(ObjectId(cardId))
        if (mongoCard != null) {
            return mongoCard
        }
        throw CardDoesNotExistException.fromCardId(cardId)
    }

    private fun MongoCard.update(card: Card): MongoCard =
        this.copy(
            key = card.key ?: this.key,
            value = card.value ?: this.value,
        )
}
