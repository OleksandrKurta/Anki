package io.github.anki.anki.repository.mongodb

import io.github.anki.anki.repository.mongodb.document.DocumentStatus
import io.github.anki.anki.repository.mongodb.document.MongoDeck
import io.github.anki.anki.repository.mongodb.document.MongoDocument
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

@Repository
class DeckRepository(
    override val mongoTemplate: MongoTemplate,
) : MongoRepository<MongoDeck>() {

    override val entityClass = MongoDeck::class.java
    override val log: Logger = LoggerFactory.getLogger(DeckRepository::class.java)

    fun findByUserIdWithStatus(userId: ObjectId, status: DocumentStatus = DocumentStatus.ACTIVE): List<MongoDeck> {
        log.info("Finding by userId = {} and status = {}", userId, status)
        return mongoTemplate.find(
            Query(
                Criteria.where(MongoDeck.USER_ID).`is`(userId).and(MongoDocument.STATUS).`is`(status),
            ),
            entityClass,
        ).also { log.info("Found by userId = {} and status = {} object = {}", userId, status, it) }
    }

    fun findByIdAndUserIdWithStatus(
        id: ObjectId,
        userId: ObjectId,
        status: DocumentStatus = DocumentStatus.ACTIVE,
    ): MongoDeck? {
        log.info("Finding by id = {} userId = {} and status = {}", id, userId, status)
        return mongoTemplate.findOne(
            Query(
                Criteria
                    .where(MongoDocument.ID)
                    .`is`(id)
                    .and(MongoDeck.USER_ID)
                    .`is`(userId)
                    .and(MongoDocument.STATUS)
                    .`is`(status),
            ),
            entityClass,
        ).also { log.info("Found by id = {} and userId = {} and status = {} object = {}", id, userId, status, it) }
    }

    fun existsByIdAndUserIdWithStatus(id: ObjectId, userId: ObjectId, status: DocumentStatus): Boolean {
        log.info("Checking existing by id = {} and userId = {} and status = {}", id, userId, status)
        return mongoTemplate.exists(
            Query(
                Criteria
                    .where(MongoDocument.ID)
                    .`is`(id)
                    .and(MongoDeck.USER_ID)
                    .`is`(userId)
                    .and(MongoDocument.STATUS)
                    .`is`(status),
            ),
            entityClass,
        ).also { log.info("Does exist by id = {} and userId = {} and status = {} object = {}", id, userId, status, it) }
    }
}
