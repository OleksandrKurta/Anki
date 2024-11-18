package io.github.anki.testing.testcontainers

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.utility.DockerImageName

object TestContainersFactory {
    fun newMongoContainer(): MongoDBContainer = MongoDBContainer("mongo:7")
    fun newNatsContainer(): GenericContainer<*> =
        GenericContainer("nats:2.8.4-alpine")
            .withExposedPorts(4222)
    fun kafkaContainer(): KafkaContainer =
        KafkaContainer(DockerImageName.parse("apache/kafka"))
}
