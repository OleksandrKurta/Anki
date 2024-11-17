package io.github.anki.testing.testcontainers

import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MongoDBContainer

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
            )
        environment.propertySources.addFirst(MapPropertySource("dynamicProperties", dynamicProperties))
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(BaseIntegrationTest::class.java)
        @Suppress("PropertyName")
        private val mongoDBContainer: MongoDBContainer = TestContainersFactory.newMongoContainer()

        @Suppress("PropertyName")
        private val natsContainer: GenericContainer<*> = TestContainersFactory.newNatsContainer()
    }
}