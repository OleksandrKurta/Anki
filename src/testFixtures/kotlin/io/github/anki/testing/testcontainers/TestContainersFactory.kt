package io.github.anki.testing.testcontainers

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MongoDBContainer

object TestContainersFactory {
    fun newMongoContainer(): MongoDBContainer = MongoDBContainer("mongo:7")
    fun newNatsContainer(): GenericContainer<*> =
        GenericContainer<Nothing>("nats:2.8.4-alpine")
            .withExposedPorts(4222)
}
