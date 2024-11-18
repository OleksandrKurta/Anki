package io.github.anki.anki.configuration

import io.github.anki.anki.api.nats.v1.deck.event.DeckEvent
import io.github.anki.anki.api.nats.v1.deck.event.KafkaTopic
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import reactor.kafka.receiver.ReceiverOptions
import reactor.kafka.sender.SenderOptions

@Configuration
@EnableKafka
class KafkaConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    @Suppress("LateinitUsage")
    private lateinit var kafkaBootstrapServers: String

    @Bean
    fun kafkaReceiverOptions(): ReceiverOptions<String, DeckEvent> {
        val receiverProperties =
            mapOf(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaBootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG to "anki",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to KEY_DESERIALIZER,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to VALUE_DESERIALIZER,
            )
        return ReceiverOptions.create(receiverProperties)
    }

    @Bean
    fun reactiveKafkaProducerTemplate(): ReactiveKafkaProducerTemplate<String, DeckEvent> {
        val producerProps =
            mapOf(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaBootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to KEY_SERIALIZER,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to VALUE_SERIALIZER,
            )
        val senderOptions: SenderOptions<String, DeckEvent> = SenderOptions.create(producerProps)
        return ReactiveKafkaProducerTemplate(senderOptions)
    }

    @Bean
    fun deckEventTopic(): NewTopic =
        TopicBuilder.name(KafkaTopic.Deck.EVENT)
            .partitions(1)
            .replicas(1)
            .build()

    companion object {
        private const val KEY_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer"
        private const val KEY_DESERIALIZER = "org.apache.kafka.common.serialization.StringDeserializer"
        private const val VALUE_SERIALIZER = "io.github.anki.anki.utils.DeckEventSerializer"
        private const val VALUE_DESERIALIZER = "io.github.anki.anki.utils.DeckEventDeserializer"
    }
}
