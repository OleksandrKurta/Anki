package io.github.anki.anki.repository.mock

import io.github.anki.anki.repository.CardsDataSource
import io.github.anki.anki.models.Card
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class MockCardsDataSource: CardsDataSource {
    private var cardsStorage = mutableListOf(
        Card(0, 0, "key", "value"),
        Card(1, 0, "key2", "value2"),
        Card(2, 1, "key3", "value3"))

    override fun retrieveCard(id: Int): Card =
        cardsStorage.firstOrNull() { it.id == id }
            ?: throw NoSuchElementException("Could not find a card with $id")

    override fun addCard(card: Card): Card {
        require(cardsStorage.none { it.id == card.id }) { "Card with id ${card.id} already exists." }
        cardsStorage.add(card)
        return card
    }

    override fun updateCard(card: Card): Card {
        val currentCard = cardsStorage.firstOrNull { it.id == card.id}
            ?: throw NoSuchElementException("Could not find a card with id ${card.id}")
        cardsStorage.remove(currentCard)
        cardsStorage.add(card)
        return card

    }

    override fun deleteCard(cardId: Int) {
        val currentCard = cardsStorage.firstOrNull { it.id == cardId }
            ?: throw NoSuchElementException("Could not find a card with id $cardId")

        cardsStorage.remove(currentCard)
    }


}
