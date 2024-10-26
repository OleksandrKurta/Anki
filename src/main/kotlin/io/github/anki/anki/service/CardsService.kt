package io.github.anki.anki.service

import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.document.DocumentStatus
import io.github.anki.anki.repository.mongodb.document.MongoCard
import io.github.anki.anki.service.exceptions.CardDoesNotExistException
import io.github.anki.anki.service.model.Card
import io.github.anki.anki.service.model.Pagination
import io.github.anki.anki.service.model.mapper.toCard
import io.github.anki.anki.service.model.mapper.toMongo
import io.github.anki.anki.service.utils.toObjectId
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
        deckService.validateUserHasPermissions(card.deckId, userId)
            .flatMap { cardRepository.insert(card.toMongo()) }
            .map(MongoCard::toCard)

    fun findCardsByDeckWithPagination(deckId: String, userId: String, pagination: Pagination): Flux<Card> =
        deckService.validateUserHasPermissions(deckId, userId)
            .flatMapMany {
                cardRepository
                    .findByDeckIdWithStatus(
                        deckId = deckId.toObjectId(),
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

    fun deleteCard(deckId: String, userId: String, cardId: String): Mono<Unit> =
        deckService.validateUserHasPermissions(deckId, userId)
            .flatMap { cardRepository.softDelete(cardId.toObjectId()) }

    private fun saveIfNotEquals(mongoCard: MongoCard, card: Card): Mono<Card> {
        val updatedMongoCard = mongoCard.update(card)
        return if (mongoCard == updatedMongoCard) {
            LOG.info("Nothing to change in Card with id {}", mongoCard.id)
            Mono.just(mongoCard.toCard())
        } else {
            cardRepository
                .save(updatedMongoCard)
                .map(MongoCard::toCard)
        }
    }

    private fun getCardById(cardId: String): Mono<MongoCard> =
        cardRepository.findByIdWithStatus(cardId.toObjectId(), DocumentStatus.ACTIVE)
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
