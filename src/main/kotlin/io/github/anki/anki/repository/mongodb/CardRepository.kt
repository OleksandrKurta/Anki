package io.github.anki.anki.repository.mongodb

import io.github.anki.anki.repository.mongodb.document.DocumentStatus
import io.github.anki.anki.repository.mongodb.document.MongoCard
import io.github.anki.anki.repository.mongodb.document.MongoDocument
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Repository
import java.util.concurrent.Future

@Repository
class CardRepository(
    override val mongoTemplate: MongoTemplate,
    @Qualifier("mongo") threadPool: ThreadPoolTaskExecutor,
) : MongoRepository<MongoCard>(threadPool) {

    override val entityClass = MongoCard::class.java
    override val log: Logger = LoggerFactory.getLogger(CardRepository::class.java)

    fun findByDeckIdWithStatus(
        deckId: ObjectId,
        status: DocumentStatus = DocumentStatus.ACTIVE,
    ): Future<List<MongoCard>> =
        threadPool.submit<List<MongoCard>> {
            log.info("Finding by deckId = {} and status = {}", deckId, status)
            mongoTemplate.find(
                Query(
                    Criteria.where(MongoCard.DECK_ID).`is`(deckId).and(MongoDocument.STATUS).`is`(status),
                ),
                entityClass,
            ).also { log.info("Found by deckId = {} and status = {} object = {}", deckId, status, it) }
        }

    fun softDeleteByDeckId(deckId: ObjectId): Future<*> =
        threadPool.submit {
            log.info("Soft deleting by deckId = {}", deckId)
            mongoTemplate.updateMulti(
                Query(Criteria.where(MongoCard.DECK_ID).`is`(deckId)),
                Update().set(MongoDocument.STATUS, DocumentStatus.DELETED),
                entityClass,
            )
            log.info("Soft deleted by deckId = {}", deckId)
        }
}
