package io.github.anki.anki.service

import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.document.DocumentStatus
import io.github.anki.anki.repository.mongodb.document.MongoCard
import io.github.anki.anki.service.exceptions.CardDoesNotExistException
import io.github.anki.anki.service.model.Card
import io.github.anki.anki.service.model.Pagination
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
        deckService.validateUserHasPermissions(card.deckId, userId)
        return cardRepository
            .insert(card.toMongo())
            .get()
            .toCard()
    }

    fun findCardsByDeckWithPagination(deckId: String, userId: String, pagination: Pagination): List<Card> {
        deckService.validateUserHasPermissions(deckId, userId)
        return cardRepository
            .findByDeckIdWithStatus(
                deckId = ObjectId(deckId),
                limit = pagination.limit,
                offset = pagination.offset,
            )
            .get()
            .map { it.toCard() }
    }

    fun updateCard(userId: String, card: Card): Card {
        deckService.validateUserHasPermissions(card.deckId, userId)
        val mongoCard: MongoCard = getCardById(card.id ?: throw IllegalArgumentException("Card Id can not be null"))
        val updatedMongoCard: MongoCard = mongoCard.update(card)
        if (mongoCard == updatedMongoCard) {
            return mongoCard.toCard()
        }
        return cardRepository.save(updatedMongoCard).get().toCard()
    }

    fun deleteCard(deckId: String, userId: String, cardId: String) {
        deckService.validateUserHasPermissions(deckId, userId)
        cardRepository.softDelete(ObjectId(cardId)).get()
    }

    private fun getCardById(cardId: String): MongoCard =
        cardRepository.findByIdWithStatus(
            ObjectId(cardId), DocumentStatus.ACTIVE,
        ).get() ?: throw CardDoesNotExistException.fromCardId(cardId)

    private fun MongoCard.update(card: Card): MongoCard =
        this.copy(
            key = card.key ?: this.key,
            value = card.value ?: this.value,
        )
}
