package io.github.anki.anki.service.model.mapper

import io.github.anki.anki.repository.mongodb.document.MongoCard
import io.github.anki.anki.service.model.Card
import org.bson.types.ObjectId

fun Card.toMongo() =
    MongoCard(
        id = this.id?.let { ObjectId(it) },
        deckId = ObjectId(this.deckId),
        cardKey = this.cardKey,
        cardValue = this.cardValue
    )

fun MongoCard.toCard() =
    Card(
        id = this.id?.toString(),
        deckId = this.deckId.toString(),
        cardKey = this.cardKey,
        cardValue = this.cardValue
    )

