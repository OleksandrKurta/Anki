package io.github.anki.anki.utils

import io.github.anki.anki.api.nats.v1.deck.event.DeckEvent
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer

class DeckEventSerializer : Serializer<DeckEvent> {
    override fun serialize(topic: String, data: DeckEvent): ByteArray = data.toByteArray()
}

class DeckEventDeserializer : Deserializer<DeckEvent> {
    override fun deserialize(topic: String, data: ByteArray): DeckEvent = DeckEvent.parseFrom(data)
}
