package io.github.anki.testing.testcontainers

import org.testcontainers.containers.MongoDBContainer

object TestContainersFactory {
    fun newMongoContainer(): MongoDBContainer {
        return MongoDBContainer("mongo:7")
    }
}
