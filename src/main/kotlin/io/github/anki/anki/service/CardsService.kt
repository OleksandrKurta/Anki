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
    suspend fun createNewCard(userId: String, card: Card): Card {
        deckService.validateUserHasPermissions(card.deckId, userId)
        return cardRepository
            .insert(card.toMongo())
            .toCard()
    }

    suspend fun findCardsByDeck(deckId: String, userId: String): List<Card> {
        deckService.validateUserHasPermissions(deckId, userId)
        return cardRepository
            .findByDeckIdWithStatus(ObjectId(deckId))
            .map { it.toCard() }
    }

    suspend fun updateCard(userId: String, card: Card): Card {
        deckService.validateUserHasPermissions(card.deckId, userId)
        val mongoCard: MongoCard = getCardById(card.id ?: throw IllegalArgumentException("Card Id can not be null"))
        val updatedMongoCard: MongoCard = mongoCard.update(card)
        if (mongoCard == updatedMongoCard) {
            return mongoCard.toCard()
        }
        return cardRepository.save(updatedMongoCard).toCard()
    }

    suspend fun deleteCard(deckId: String, userId: String, cardId: String) {
        deckService.validateUserHasPermissions(deckId, userId)
        cardRepository.softDelete(ObjectId(cardId))
    }

    private suspend fun getCardById(cardId: String): MongoCard =
        cardRepository.findById(ObjectId(cardId)) ?: throw CardDoesNotExistException.fromCardId(cardId)

    private fun MongoCard.update(card: Card): MongoCard =
        this.copy(
            key = card.key ?: this.key,
            value = card.value ?: this.value,
        )
}
