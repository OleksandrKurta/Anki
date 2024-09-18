package io.github.anki.anki.repository.mongodb

import io.github.anki.anki.configuration.ThreadPoolsConfiguration
import io.github.anki.anki.repository.mongodb.document.DocumentStatus
import io.github.anki.anki.repository.mongodb.document.MongoCard
import io.github.anki.anki.repository.mongodb.document.MongoDocument
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.task.AsyncTaskExecutor
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import java.util.concurrent.CompletableFuture

@Repository
class CardRepository(
    override val mongoTemplate: MongoTemplate,
    @Qualifier(ThreadPoolsConfiguration.MONGO_THREAD_POOL_QUALIFIER) override val threadPool: AsyncTaskExecutor,
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
}
