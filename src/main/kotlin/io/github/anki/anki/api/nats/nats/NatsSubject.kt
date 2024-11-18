package io.github.anki.anki.api.nats.nats

object NatsSubject {
    private const val BASE_PREFIX = "v1.anki"

    object Card {
        private const val CARD_PREFIX = "$BASE_PREFIX.card"
        const val CARD_DELETE_COMMAND = "$CARD_PREFIX.delete"
    }
}
