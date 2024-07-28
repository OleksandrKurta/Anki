package io.github.anki.testing

import org.testcontainers.containers.MongoDBContainer


object TestContainersFactory {
    fun newMongoContainer(): MongoDBContainer {
        return MongoDBContainer("mongo:7")
    }
}
