package io.github.anki.anki.repository.mongodb

import io.github.anki.anki.configuration.AppConfiguration
import io.github.anki.anki.repository.mongodb.document.DocumentStatus
import io.github.anki.anki.repository.mongodb.document.MongoDocument
import io.github.anki.anki.repository.mongodb.document.MongoRateLog
import org.bson.types.ObjectId
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
class RateLogRepository(
    override val mongoTemplate: MongoTemplate,
    @Qualifier(AppConfiguration.MONGO_THREAD_POOL_QUALIFIER) override val threadPool: AsyncTaskExecutor,
) : MongoRepository<MongoRateLog>(threadPool) {

    override val entityClass = MongoRateLog::class.java
    override val log: Logger = LoggerFactory.getLogger(RateLogRepository::class.java)

    fun findAllRates(
        userId: ObjectId,
        learnObjectId: ObjectId,
        status: DocumentStatus = DocumentStatus.ACTIVE,
    ): CompletableFuture<List<MongoRateLog>> =
        threadPool.submitCompletable<List<MongoRateLog>> {
            log.info(
                "Finding rate by userId = {} and LearnObjectId = {} and status = {}",
                userId,
                learnObjectId,
                status,
            )
            mongoTemplate.find(
                Query(
                    Criteria
                        .where(MongoRateLog.USER_ID).`is`(userId)
                        .and(MongoRateLog.LEARNING_ITEM_ID).`is`(learnObjectId)
                        .and(MongoDocument.DOCUMENT_STATUS).`is`(status),
                ),
                entityClass,
            ).also {
                log.info(
                    "Found rate by userId = {} and leardObjectId = {} and status = {} - {}",
                    userId,
                    learnObjectId,
                    status,
                    it,
                )
            }
        }
}
