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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class CardsService(
    private val cardRepository: CardRepository,
    private val deckService: DeckService,
) {
    fun createNewCard(userId: String, card: Card): Mono<Card> =
        deckService
            .validateUserHasPermissions(card.deckId, userId)
            .doOnNext { LOG.info("After Permission validation $it") }
            .flatMap { cardRepository.insert(card.toMongo()) }
            .doOnNext { LOG.info("Inserted object in Service $it") }
            .map(MongoCard::toCard)
            .doOnNext { LOG.info("Mapped Card $it") }

    fun findCardsByDeckWithPagination(deckId: String, userId: String, pagination: Pagination): Flux<Card> =
        deckService.validateUserHasPermissions(deckId, userId)
            .flatMapMany {
                cardRepository
                    .findByDeckIdWithStatus(
                        deckId = ObjectId(deckId),
                        limit = pagination.limit,
                        offset = pagination.offset,
                    )
            }
            .map(MongoCard::toCard)

    fun updateCard(userId: String, card: Card): Mono<Card> {
        card.id ?: return Mono.error(IllegalArgumentException("Card Id can not be null"))
        return deckService.validateUserHasPermissions(card.deckId, userId)
            .flatMap { getCardById(card.id) }
            .flatMap { saveIfNotEquals(it, card) }
    }

    fun deleteCard(deckId: String, userId: String, cardId: String): Mono<Void> =
        deckService.validateUserHasPermissions(deckId, userId)
            .flatMap { cardRepository.softDelete(ObjectId(cardId)) }

    private fun saveIfNotEquals(mongoCard: MongoCard, card: Card): Mono<Card> {
        val updatedMongoCard = mongoCard.update(card)
        if (mongoCard == updatedMongoCard) {
            return Mono.just(mongoCard.toCard())
        }
        return cardRepository
            .save(updatedMongoCard)
            .map(MongoCard::toCard)
    }

    private fun getCardById(cardId: String): Mono<MongoCard> =
        cardRepository.findByIdWithStatus(ObjectId(cardId), DocumentStatus.ACTIVE)
            .switchIfEmpty(Mono.error(CardDoesNotExistException.fromCardId(cardId)))

    private fun MongoCard.update(card: Card): MongoCard =
        this.copy(
            key = card.key ?: this.key,
            value = card.value ?: this.value,
        )

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(CardsService::class.java)
    }
}
