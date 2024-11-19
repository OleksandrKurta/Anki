package io.github.anki.anki.service

import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.document.DocumentStatus
import io.github.anki.anki.repository.mongodb.document.MongoCard
import io.github.anki.anki.service.exceptions.CardDoesNotExistException
import io.github.anki.anki.service.model.Pagination
import io.github.anki.anki.service.model.mapper.toCardEntity
import io.github.anki.anki.service.model.mapper.toMongo
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import service.model.CardLearningEntity
import java.time.Instant

@Service
class CardsService(
    private val cardRepository: CardRepository,
    private val deckService: DeckService,
) {
    fun createNewCard(userId: String, card: CardLearningEntity): CardLearningEntity {
        deckService.validateUserHasPermissions(card.deckId, userId)
        return cardRepository
            .insert(card.toMongo())
            .get()
            .toCardEntity()
    }

    fun findCardsByDeckWithPagination(deckId: String, userId: String, pagination: Pagination): List<CardLearningEntity> {
        deckService.validateUserHasPermissions(deckId, userId)
        return cardRepository
            .findByDeckIdWithStatus(
                deckId = ObjectId(deckId),
                limit = pagination.limit,
                offset = pagination.offset,
            )
            .get()
            .map { it.toCardEntity() }
    }

    fun updateCardEntity(userId: String, card: CardLearningEntity): CardLearningEntity {
        deckService.validateUserHasPermissions(card.deckId, userId)
        val mongoCard: MongoCard = getCardById(card.id ?: throw IllegalArgumentException("Card Id can not be null"))
        val updatedMongoCard: MongoCard = mongoCard.update(card)
        if (mongoCard == updatedMongoCard) {
            return mongoCard.toCardEntity()
        }
        return cardRepository.save(updatedMongoCard).get().toCardEntity()
    }

    fun deleteCard(deckId: String, userId: String, cardId: String) {
        deckService.validateUserHasPermissions(deckId, userId)
        cardRepository.softDelete(ObjectId(cardId)).get()
    }

    private fun getCardById(cardId: String): MongoCard =
        cardRepository.findByIdWithStatus(
            ObjectId(cardId), DocumentStatus.ACTIVE,
        ).get() ?: throw CardDoesNotExistException.fromCardId(cardId)

    private fun MongoCard.update(card: CardLearningEntity): MongoCard =
        this.copy(
            key = (card.hint ?: this.key).toString(),
            value = (card.answer ?: this.value).toString(),
            lastRateId = ObjectId(card.rateId ?: this.value),
            lastLearn = card.lastLearn ?: Instant.now(),
        )
}
