package io.github.anki.anki.repository.mongodb.model

import io.github.anki.anki.controller.model.CardDto
import io.github.anki.anki.service.model.Card
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document


@Document(collection = "card")
data class MongoCard(
    val parentCollectionId: String,
    val cardKey: String,
    val cardValue: String) {

    private var _id: ObjectId = ObjectId()

    var id: ObjectId
        get() = _id
        set(value) {
            _id = value
        }

    fun toEntity(): Card {
        val card =  Card(parentCollectionId, cardKey, cardValue)
        card.id = id.toString()
        return card
    }

    fun toDto(): CardDto {
        val dto = CardDto(parentCollectionId, cardKey, cardValue)
        dto.id = id.toString()
        return dto
    }
}
