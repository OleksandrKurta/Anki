package io.github.anki.anki.service.model.mapper

import io.github.anki.anki.repository.mongodb.document.MongoDeck
import io.github.anki.anki.service.model.Deck
import io.github.anki.anki.service.utils.toObjectId

fun Deck.toMongo(): MongoDeck =
    MongoDeck(
        id = this.id?.toObjectId(),
        userId = this.userId.toObjectId(),
        name = this.name,
        description = this.description,
    )

fun MongoDeck.toDeck() =
    Deck(
        id = this.id.toString(),
        userId = this.userId.toString(),
        name = this.name,
        description = this.description,
    )
