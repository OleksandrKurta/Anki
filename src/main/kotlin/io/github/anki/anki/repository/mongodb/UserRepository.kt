package io.github.anki.anki.repository.mongodb

import io.github.anki.anki.configuration.ThreadPoolsConfiguration
import io.github.anki.anki.repository.mongodb.document.DocumentStatus
import io.github.anki.anki.repository.mongodb.document.MongoDocument
import io.github.anki.anki.repository.mongodb.document.MongoUser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.task.AsyncTaskExecutor
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import java.util.concurrent.CompletableFuture

@Repository
class UserRepository(
    override val mongoTemplate: MongoTemplate,
    @Qualifier(ThreadPoolsConfiguration.MONGO_THREAD_POOL_QUALIFIER) override val threadPool: AsyncTaskExecutor,
) : MongoRepository<MongoUser>(threadPool) {
    override val entityClass = MongoUser::class.java
    override val log: Logger = LoggerFactory.getLogger(UserRepository::class.java)

    fun findByUserName(
        userName: String,
        status: DocumentStatus = DocumentStatus.ACTIVE,
    ): CompletableFuture<MongoUser?> =
        threadPool.submitCompletable<MongoUser?> {
            log.info("Finding by userName = {} with status = {}", userName, status)
            mongoTemplate.findOne(
                Query(
                    Criteria
                        .where(MongoUser.USER_NAME)
                        .`is`(userName)
                        .and(MongoDocument.DOCUMENT_STATUS)
                        .`is`(status),
                ),
                entityClass,
            ).also { log.info("Found by userName = {} and status = {} object = {}", userName, status, it) }
        }

    fun existsByUserName(
        userName: String?,
        status: DocumentStatus = DocumentStatus.ACTIVE,
    ): CompletableFuture<Boolean> =
        threadPool.submitCompletable<Boolean> {
            log.info("Check exists by userName = {} with status = {}", userName, status)
            mongoTemplate.exists(
                Query(
                    Criteria
                        .where(MongoUser.USER_NAME)
                        .`is`(userName)
                        .and(MongoDocument.DOCUMENT_STATUS)
                        .`is`(status),
                ),
                entityClass,
            ).also { log.info("Check exists userName = {} and status = {} object = {}", userName, status, it) }
        }
}
