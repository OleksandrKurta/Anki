package io.github.anki.testing.testcontainers

import org.springframework.test.context.DynamicPropertyRegistry
import org.testcontainers.containers.MongoDBContainer

fun DynamicPropertyRegistry.with(testContainer: MongoDBContainer) {
    add("spring.data.mongodb.uri") { testContainer.replicaSetUrl }
}
