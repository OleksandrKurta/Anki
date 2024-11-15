package io.github.anki.anki.configuration

import io.nats.client.Connection
import io.nats.client.Nats
import io.nats.client.Options
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class NatsConfiguration {

    @Value("\${nats.server.url}")
    private val natsServerUrl: String? = null

    @Bean
    fun natsConnection(): Connection {
        val options: Options =
            Options.Builder()
                .server(natsServerUrl)
                .build()
        return Nats.connect(options)
    }
}
