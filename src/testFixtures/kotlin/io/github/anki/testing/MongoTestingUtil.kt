package io.github.anki.testing

import org.springframework.test.context.DynamicPropertyRegistry
import org.testcontainers.containers.MongoDBContainer

fun newMongoContainer(): MongoDBContainer {
    return MongoDBContainer("mongo:7")
}

fun DynamicPropertyRegistry.setMongo(uri: String) {
    add("spring.data.mongodb.uri") { uri }
}
