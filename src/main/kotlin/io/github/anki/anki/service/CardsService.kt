package io.github.anki.anki.service

import io.github.anki.anki.repository.CardsDataSource
import io.github.anki.anki.models.Card
import org.springframework.stereotype.Service


@Service
class CardsService(private val dataSource: CardsDataSource) {

    fun getCard(id: Int): Card = dataSource.retrieveCard(id)

    fun addCard(card: Card): Card = dataSource.addCard(card)

    fun updateCard(card: Card): Card = dataSource.updateCard(card)

    fun deleteCard(cardId: Int): Unit = dataSource.deleteCard(cardId)

}
