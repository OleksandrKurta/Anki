package io.github.anki.anki.service
import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.service.model.Card
import io.github.anki.anki.service.model.mapper.toCard
import io.github.anki.anki.service.model.mapper.toMongo
import org.slf4j.LoggerFactory
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

    fun deleteCard(cardId: String) {
        LOG.info("Deleting card with id: {}", cardId)
        cardRepository.deleteById(cardId)
        LOG.info("Successfully deleted card with id: {}", cardId)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(CardsService::class.java)
    }
}
