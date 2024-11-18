package io.github.anki.anki.service.model.mapper

import io.github.anki.anki.api.kafka.v1.deck.DeckEvent
import io.github.anki.anki.repository.mongodb.document.MongoDeckEvent
import io.github.anki.anki.service.utils.toObjectId

fun DeckEvent.toMongoDeckEvent() =
    MongoDeckEvent(
        deckId = this.deckId.toObjectId(),
        event = this.deckEventType.name,
    )
