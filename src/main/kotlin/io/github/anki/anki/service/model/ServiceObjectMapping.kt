package io.github.anki.anki.service.model

import io.github.anki.anki.controller.model.CardDto
import io.github.anki.anki.repository.mongodb.model.MongoCard
import org.bson.types.ObjectId

fun mapServiceToDto(card: Card): CardDto {
        return CardDto(
            card.id,
            card.parentCollectionId,
            card.cardKey,
            card.cardValue)
}

fun mapServiceToRepository(serviceCard: Card): MongoCard {
        val mongoCard = MongoCard(
            parentCollectionId = serviceCard.parentCollectionId,
            cardKey = serviceCard.cardKey,
            cardValue = serviceCard.cardValue)
        if (serviceCard.id != null) {
            mongoCard.id = ObjectId(serviceCard.id)
        }
        return mongoCard
}
