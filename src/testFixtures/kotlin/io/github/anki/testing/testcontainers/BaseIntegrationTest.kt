package io.github.anki.testing.testcontainers

import jakarta.annotation.PostConstruct
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.kafka.KafkaContainer

@TestConfiguration
class BaseIntegrationTest(
    private val environment: ConfigurableEnvironment,
) {

    @PostConstruct
    fun init() {
        val dynamicProperties =
            mapOf(
                "spring.data.mongodb.uri" to mongoDBContainer.replicaSetUrl,
                "nats.server.url" to natsContainer.host + ":" + natsContainer.getMappedPort(4222),
                "spring.kafka.bootstrap-servers" to kafkaContainer.bootstrapServers
            )
        environment.propertySources.addFirst(MapPropertySource("dynamicProperties", dynamicProperties))
    }

    companion object {
        @Suppress("PropertyName")
        private val mongoDBContainer: MongoDBContainer = TestContainersFactory.newMongoContainer().apply { start() }

        @Suppress("PropertyName")
        private val natsContainer: GenericContainer<*> = TestContainersFactory.newNatsContainer().apply { start() }

        @Suppress("PropertyName")
        private val kafkaContainer: KafkaContainer = TestContainersFactory.kafkaContainer().apply { start() }
    }
}