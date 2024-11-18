package io.github.anki.anki.service.model.mapper

import io.github.anki.anki.api.nats.v1.deck.event.DeckEvent
import io.github.anki.anki.repository.mongodb.document.MongoDeckEvent
import io.github.anki.anki.service.utils.toObjectId

fun DeckEvent.toMongoDeckEvent() =
    MongoDeckEvent(
        deckId = this.deckId.toObjectId(),
        event = this.deckEventType.name,
    )
