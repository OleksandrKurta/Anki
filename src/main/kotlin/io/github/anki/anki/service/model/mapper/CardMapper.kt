package io.github.anki.anki.service.model.mapper

import io.github.anki.anki.repository.mongodb.document.MongoCard
import io.github.anki.anki.service.model.Card
import io.github.anki.anki.service.utils.toObjectId

fun Card.toMongo() =
    MongoCard(
        id = this.id?.toObjectId(),
        deckId = this.deckId.toObjectId(),
        key = this.key,
        value = this.value,
    )

fun MongoCard.toCard() =
    Card(
        id = this.id?.toString(),
        deckId = this.deckId.toString(),
        key = this.key,
        value = this.value,
    )
