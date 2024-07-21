package io.github.anki.anki.service

import io.github.anki.anki.controller.model.CardDto
import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.model.MongoCard
import io.github.anki.anki.service.model.Card
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import java.util.*


@Service
class CardsService(private val dataSource: CardRepository) {

    fun getCards(collectionId: String): List<Card> {
        val dtoList = mutableListOf<Card>()
        val models =  dataSource.findByParentCollectionId(collectionId)
        for (model in models)
            dtoList.add(model.toEntity())
        return dtoList
    }

    fun addCard(card: Card): Card {
        val cardModel = dataSource.insert(card.toModel())
        return cardModel.toEntity()
    }

    fun updateCard(card: Card): Card {
        return dataSource.save(card.toModel()).toEntity()
    }

    fun deleteCard(cardId: String): String {
        dataSource.deleteById(cardId)
        return cardId
    }
}
