package io.github.anki.testing.testcontainers

import org.testcontainers.containers.MongoDBContainer

object TestContainersFactory {
    fun newMongoContainer(): MongoDBContainer = MongoDBContainer("mongo:7")
}
