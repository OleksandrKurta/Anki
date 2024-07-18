package io.github.anki.anki.repository

import io.github.anki.anki.models.Card

interface CardsDataSource {

    fun retrieveCard(id: Int): Card

    fun addCard(card: Card): Card

    fun updateCard(card: Card): Card

    fun deleteCard(cardId: Int)
}
