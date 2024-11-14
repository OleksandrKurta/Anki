package io.github.anki.testing.testcontainers

import org.springframework.test.context.DynamicPropertyRegistry
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MongoDBContainer

fun DynamicPropertyRegistry.with(testContainer: MongoDBContainer) {
    add("spring.data.mongodb.uri") { testContainer.replicaSetUrl }
}

fun DynamicPropertyRegistry.withNats(testContainer: GenericContainer<*>) {
    add("nats.server.url") { testContainer.host + ":" + testContainer.getMappedPort(4222) }
}
