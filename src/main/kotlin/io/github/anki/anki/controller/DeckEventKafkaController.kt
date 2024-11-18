package io.github.anki.anki.controller

import io.github.anki.anki.api.nats.kafka.KafkaTopic
import io.github.anki.anki.configuration.KafkaTopicsManager
import io.github.anki.anki.service.DeckEventService
import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller

@Controller
class DeckEventKafkaController(
    private val deckEventService: DeckEventService,
    private val kafkaTopicsManager: KafkaTopicsManager,
) : KafkaController {

    @PostConstruct
    override fun subscribeToTopic() {
        kafkaTopicsManager.getKafkaConsumerTemplate(KAFKA_TOPIC)
            .receive()
            .doOnNext { LOG.info("Received message from kafka: topic={}, message={}", KAFKA_TOPIC, it) }
            .flatMap { deckEventService.saveDeckEvent(it.value()) }
            .subscribe()
    }

    companion object {
        private const val KAFKA_TOPIC: String = KafkaTopic.Deck.EVENT
        private val LOG: Logger = LoggerFactory.getLogger(DeckEventService::class.java)
    }
}
