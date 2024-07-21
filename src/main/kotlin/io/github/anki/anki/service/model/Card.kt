package io.github.anki.anki.service.model

import io.github.anki.anki.controller.model.CardDto
import io.github.anki.anki.repository.mongodb.model.MongoCard
import org.bson.types.ObjectId

class Card (
    var parentCollectionId: String,
    var cardKey: String,
    var cardValue: String,
) {
    private var _id: String? = null

    var id: String?
        get() = _id
        set(value) {
            _id = value
        }

    fun toDto(): CardDto {
        val dto = CardDto(parentCollectionId, cardValue, cardKey)
        if (_id != null) {
            dto.id = _id
        }
        return dto
    }

    fun toModel(): MongoCard {
        val model = MongoCard(parentCollectionId, cardValue, cardKey)
        if (_id != null) {
            model.id = ObjectId(_id)
        }
        return model
    }
}

