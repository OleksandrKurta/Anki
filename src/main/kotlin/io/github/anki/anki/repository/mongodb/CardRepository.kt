package io.github.anki.anki.repository.mongodb

import io.github.anki.anki.configuration.AppConfiguration
import io.github.anki.anki.repository.mongodb.document.DocumentStatus
import io.github.anki.anki.repository.mongodb.document.MongoCard
import io.github.anki.anki.repository.mongodb.document.MongoDocument
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.task.AsyncTaskExecutor
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.temporal.TemporalAmount
import java.util.concurrent.CompletableFuture
import kotlin.time.Duration

@Repository
class CardRepository(
    override val mongoTemplate: MongoTemplate,
    @Qualifier(AppConfiguration.MONGO_THREAD_POOL_QUALIFIER) override val threadPool: AsyncTaskExecutor,
) : MongoRepository<MongoCard>(threadPool) {

    override val entityClass = MongoCard::class.java
    override val log: Logger = LoggerFactory.getLogger(CardRepository::class.java)

    fun findByDeckIdWithStatus(
        deckId: ObjectId,
        status: DocumentStatus = DocumentStatus.ACTIVE,
        limit: Int = 50,
        offset: Int = 0,
    ): CompletableFuture<List<MongoCard>> =
        threadPool.submitCompletable<List<MongoCard>> {
            log.info("Finding by deckId = {} and status = {}", deckId, status)
            mongoTemplate.find(
                Query(
                    Criteria.where(MongoCard.DECK_ID).`is`(deckId).and(MongoDocument.DOCUMENT_STATUS).`is`(status),
                ).limit(limit).skip(offset.toLong()),
                entityClass,
            ).also {
                log.info(
                    "Found by deckId = {} and status = {} and limit = {} and offset = {} object = {}",
                    deckId,
                    status,
                    limit,
                    offset,
                    it,
                )
            }
        }

    fun softDeleteByDeckId(deckId: ObjectId): CompletableFuture<Void> =
        threadPool.submitCompletable {
            log.info("Soft deleting by deckId = {}", deckId)
            mongoTemplate.updateMulti(
                Query(Criteria.where(MongoCard.DECK_ID).`is`(deckId)),
                Update().set(MongoDocument.DOCUMENT_STATUS, DocumentStatus.DELETED),
                entityClass,
            )
            log.info("Soft deleted by deckId = {}", deckId)
        }

    fun findByDeckIdAscDateWithLimit(
        deckId: ObjectId,
        limit: Int
): CompletableFuture<List<MongoCard>> =
    threadPool.submitCompletable<List<MongoCard>> {
        log.info("Finding by deckId = {} with modifiedAt >= {} and limit = {}", deckId, limit)
        mongoTemplate.find(
            Query(
                Criteria.where(MongoCard.DECK_ID)
                    .`is`(deckId)
            ).limit(limit)
             .with(Sort.by(Sort.Direction.ASC, MongoDocument.MODIFIED_AT)),
            entityClass
        ).also {
            log.info(
                "Found by deckId = {} with modifiedAt >= {} and limit = {} object = {}",
                deckId,
                limit,
                it
            )
        }
    }
}
