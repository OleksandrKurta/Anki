package io.github.anki.anki.service
import io.github.anki.anki.exception.ResourceNotFoundException
import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.model.mapRepositoryToService
import io.github.anki.anki.service.model.Card
import io.github.anki.anki.service.model.mapServiceToRepository
import org.springframework.stereotype.Service



@Service
class CardsService(
    private val dataSource: CardRepository,
) {
    fun addCard(card: Card): Card {
        val cardModel = dataSource.insert(mapServiceToRepository(card))
        return mapRepositoryToService(cardModel)
    }

    fun updateCard(card: Card): Card? {
        if (dataSource.existsById(mapServiceToRepository(card).id.toString()))
            return mapRepositoryToService(dataSource.save(mapServiceToRepository(card)))
        return null
    }

    fun deleteCard(cardId: String): String? {
        if (dataSource.existsById(cardId)){
            dataSource.deleteById(cardId)
            return cardId
        }
        return null
    }
}
