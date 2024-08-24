package io.github.anki.anki.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@Configuration
class ThreadPoolConfiguration {

    @Suppress("MagicNumber")
    private val threadPoolCPUMultiplier = 50

    @Bean
    fun taskExecutor(): ThreadPoolTaskExecutor {
        val threadPoolCapacity = Runtime.getRuntime().availableProcessors() * threadPoolCPUMultiplier
        val pool =
            ThreadPoolTaskExecutor().apply {
                corePoolSize = threadPoolCapacity
                maxPoolSize = threadPoolCapacity
                setThreadNamePrefix("Async-")
                initialize()
            }
        return pool
    }
}
