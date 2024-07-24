package io.github.anki.anki.controller.model

import io.github.anki.anki.exception.ResourceNotFoundException
import io.github.anki.anki.repository.mongodb.model.MongoCard
import io.github.anki.anki.service.model.Card
import org.bson.types.ObjectId


fun mapDtoToService(dto: CardDto): Card {
        return Card(dto.id, dto.parentCollectionId, dto.cardKey, dto.cardValue)
}

fun mapNewDtoToCard(dto: NewCardDto): Card {
        return Card(parentCollectionId = dto.parentCollectionId,
                cardKey = dto.cardKey,
                cardValue = dto.cardValue)
}

fun mapDtoToRepository(dto: CardDto?): MongoCard {
        if (dto == null) { throw ResourceNotFoundException("No dto given to mapper") }
        val mongoCard = MongoCard(parentCollectionId = dto.parentCollectionId,
                cardKey = dto.cardKey,
                cardValue = dto.cardValue)
        if (dto.id != null) {
                mongoCard.id = ObjectId(dto.id)
        }
        return mongoCard
}
