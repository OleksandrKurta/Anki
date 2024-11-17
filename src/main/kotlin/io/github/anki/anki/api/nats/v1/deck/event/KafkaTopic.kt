package io.github.anki.anki.api.nats.v1.deck.event

object KafkaTopic {
    private const val BASE_PREFIX = "v1.anki"

    object Deck {
        private const val DECK_PREFIX = "$BASE_PREFIX.deck"

        object Event {
            const val CREATE = "$DECK_PREFIX.create"
            const val UPDATE = "$DECK_PREFIX.update"
            const val DELETE = "$DECK_PREFIX.delete"
        }
    }
}
