package io.github.anki.anki.controller.model
import io.github.anki.anki.repository.mongodb.model.MongoCard
import io.github.anki.anki.service.model.Card
import org.bson.types.ObjectId


class CardDto (
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


    fun toEntity(): Card {
        val entity = Card(parentCollectionId, cardValue, cardKey)
        if (_id != null) {
            entity.id = _id
        }
        return entity
    }

    fun toModel(): MongoCard {
        val model = MongoCard(parentCollectionId, cardKey, cardValue)
        if (_id != null) {
            model.id = ObjectId(_id)
        }
        return model
    }
}

