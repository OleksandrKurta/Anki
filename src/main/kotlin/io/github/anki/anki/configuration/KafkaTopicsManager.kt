package io.github.anki.anki.configuration

import io.github.anki.anki.api.kafka.v1.deck.DeckEvent
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import org.springframework.stereotype.Component
import reactor.kafka.receiver.ReceiverOptions
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

@Component
class KafkaTopicsManager(
    private val kafkaReceiverOptions: ReceiverOptions<String, DeckEvent>,
) {
    private val kafkaConsumerTemplates: ConcurrentMap<String, ReactiveKafkaConsumerTemplate<String, DeckEvent>> =
        ConcurrentHashMap()

    fun getKafkaConsumerTemplate(topic: String): ReactiveKafkaConsumerTemplate<String, DeckEvent> =
        kafkaConsumerTemplates.computeIfAbsent(
            topic,
            { ReactiveKafkaConsumerTemplate(kafkaReceiverOptions.subscription(listOf(topic))) },
        )
}
