package io.github.anki.anki.configuration

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.AsyncTaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@Configuration
class ThreadPoolsConfiguration {

    @Bean
    @Qualifier(MONGO_THREAD_POOL_QUALIFIER)
    fun mongoThreadPoolTaskExecutor(): AsyncTaskExecutor {
        return ThreadPoolTaskExecutor().apply {
            @Suppress("MagicNumber")
            corePoolSize = MONGO_THREAD_POOL_SIZE
            maxPoolSize = corePoolSize
            setThreadNamePrefix(MONGO_THREAD_NAME_PREFIX)
            initialize()
        }
    }

    companion object {
        const val MONGO_THREAD_NAME_PREFIX: String = "Mongo-"
        const val MONGO_THREAD_POOL_QUALIFIER: String = "mongo-thread-pool"
        val MONGO_THREAD_POOL_SIZE: Int = Runtime.getRuntime().availableProcessors() * 50
    }
}
