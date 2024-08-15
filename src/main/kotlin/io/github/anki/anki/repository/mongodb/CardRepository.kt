package io.github.anki.anki.repository.mongodb

import io.github.anki.anki.repository.mongodb.document.DocumentStatus
import io.github.anki.anki.repository.mongodb.document.MongoCard
import io.github.anki.anki.repository.mongodb.document.MongoDocument
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingle
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository

@Repository
class CardRepository(
    override val mongoTemplate: ReactiveMongoTemplate,
) : MongoRepository<MongoCard>() {

    override val entityClass = MongoCard::class.java
    override val log: Logger = LoggerFactory.getLogger(CardRepository::class.java)

    suspend fun findByDeckIdWithStatus(
        deckId: ObjectId,
        status: DocumentStatus = DocumentStatus.ACTIVE,
    ): List<MongoCard> {
        log.info("Finding by deckId = {} and status = {}", deckId, status)
        return mongoTemplate
            .find(
                Query(
                    Criteria.where(MongoCard.DECK_ID).`is`(deckId).and(MongoDocument.STATUS).`is`(status),
                ),
                entityClass,
            )
            .collectList()
            .awaitSingle()
            .also { log.info("Found by deckId = {} and status = {} object = {}", deckId, status, it) }
    }

    suspend fun softDeleteByDeckId(deckId: ObjectId) {
        log.info("Soft deleting by deckId = {}", deckId)
        mongoTemplate.updateMulti(
            Query(Criteria.where(MongoCard.DECK_ID).`is`(deckId)),
            Update().set(MongoDocument.STATUS, DocumentStatus.DELETED),
            entityClass,
        ).awaitSingle()
        log.info("Soft deleted by deckId = {}", deckId)
    }
}
