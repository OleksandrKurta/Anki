package io.github.anki.anki.configuration

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@Configuration
class ThreadPoolsConfiguration {
    private val availableProcessors = Runtime.getRuntime().availableProcessors()

    @Bean
    @Qualifier("mongo")
    fun mongoThreadPoolTaskExecutor(): ThreadPoolTaskExecutor {
        return ThreadPoolTaskExecutor().apply {
            @Suppress("MagicNumber")
            corePoolSize = availableProcessors * 50
            maxPoolSize = corePoolSize
            setThreadNamePrefix("Mongo-")
            initialize()
        }
    }
}
