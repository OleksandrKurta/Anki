package io.github.anki.anki.api.nats.v1.deck.event

object KafkaTopic {
    private const val BASE_PREFIX = "v1.anki"

    object Deck {
        private const val DECK_PREFIX = "$BASE_PREFIX.deck"

        const val EVENT = "$DECK_PREFIX.event"
    }
}
