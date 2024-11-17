package io.github.anki.testing.testcontainers

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.kafka.KafkaContainer

object TestContainersFactory {
    fun newMongoContainer(): MongoDBContainer = MongoDBContainer("mongo:7").apply { start() }
    fun newNatsContainer(): GenericContainer<*> =
        GenericContainer("nats:2.8.4-alpine")
            .withExposedPorts(4222)
            .apply { start() }
    fun kafkaContainer(): KafkaContainer = KafkaContainer("bitnami/kafka:3.5").apply { start() }
}
