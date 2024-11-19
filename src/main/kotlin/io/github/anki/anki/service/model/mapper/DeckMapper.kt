package io.github.anki.anki.service.model.mapper

import io.github.anki.anki.repository.mongodb.document.MongoDeck
import io.github.anki.anki.service.model.Deck
import org.bson.types.ObjectId

fun Deck.toMongo(): MongoDeck {
    return MongoDeck(
        userId = ObjectId(this.userId),
        name = this.name,
        description = this.description,
        presetId = ObjectId(this.presetId),
    )
}


fun MongoDeck.toDeck() =
    Deck(
        id = this.id.toString(),
        userId = this.userId.toString(),
        name = this.name,
        description = this.description,
        presetId = this.presetId.toString(),
    )
