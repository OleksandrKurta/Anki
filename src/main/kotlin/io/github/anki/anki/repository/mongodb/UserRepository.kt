package io.github.anki.anki.repository.mongodb

import io.github.anki.anki.configuration.ThreadPoolsConfiguration
import io.github.anki.anki.repository.mongodb.document.DocumentStatus
import io.github.anki.anki.repository.mongodb.document.MongoDocument
import io.github.anki.anki.repository.mongodb.document.MongoUser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.task.AsyncTaskExecutor
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class UserRepository(
    override val mongoTemplate: ReactiveMongoTemplate,
    @Qualifier(ThreadPoolsConfiguration.MONGO_THREAD_POOL_QUALIFIER) override val threadPool: AsyncTaskExecutor,
) : MongoRepository<MongoUser>(threadPool) {
    override val entityClass = MongoUser::class.java
    override val log: Logger = LoggerFactory.getLogger(UserRepository::class.java)

    fun findByUserName(
        userName: String,
        status: DocumentStatus = DocumentStatus.ACTIVE,
    ): Mono<MongoUser?> =
        mongoTemplate.findOne(
            Query(
                Criteria
                    .where(MongoUser.USER_NAME)
                    .`is`(userName)
                    .and(MongoDocument.DOCUMENT_STATUS)
                    .`is`(status),
            ),
            entityClass,
        )
            .doFirst { log.info("Finding by userName = {} with status = {}", userName, status) }
            .doOnNext { obj ->
                log.info(
                    "Found by userName = {} and status = {} object = {}",
                    userName,
                    status,
                    obj,
                )
            }

    fun existsByUserName(
        userName: String?,
        status: DocumentStatus = DocumentStatus.ACTIVE,
    ): Mono<Boolean> =
        mongoTemplate.exists(
            Query(
                Criteria
                    .where(MongoUser.USER_NAME)
                    .`is`(userName)
                    .and(MongoDocument.DOCUMENT_STATUS)
                    .`is`(status),
            ),
            entityClass,
        )
            .doFirst { log.info("Check exists by userName = {} with status = {}", userName, status) }
            .doOnNext { exists ->
                log.info(
                    "Check exists userName = {} and status = {} object = {}",
                    userName,
                    status,
                    exists,
                )
            }
}
