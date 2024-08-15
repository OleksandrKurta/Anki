package io.github.anki.anki.repository.mongodb

import io.github.anki.anki.repository.mongodb.document.DocumentStatus
import io.github.anki.anki.repository.mongodb.document.MongoDocument
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update

abstract class MongoRepository<T : MongoDocument> {
    protected abstract val mongoTemplate: ReactiveMongoTemplate
    protected abstract val entityClass: Class<T>
    protected abstract val log: Logger

    suspend fun insert(obj: T): T {
        log.info("Inserting {}", obj)
        return mongoTemplate
            .insert(obj)
            .awaitSingle()
            .also { log.info("Inserted object = {}", it) }
    }

    suspend fun insert(objects: Iterable<T>): List<T> {
        log.info("Inserting {}", objects)
        return mongoTemplate
            .insertAll(objects.toList())
            .collectList()
            .awaitSingle()
            .also { log.info("Inserted object = {}", it) }
    }

    suspend fun save(obj: T): T {
        log.info("Saving {}", obj)
        return mongoTemplate.save(obj).awaitSingle().also { log.info("Saved object = {}", it) }
    }

    suspend fun softDelete(id: ObjectId) {
        log.info("Soft deleting by id = {}", id)
        mongoTemplate.updateFirst(
            Query(Criteria.where(MongoDocument.ID).`is`(id)),
            Update().set(MongoDocument.STATUS, DocumentStatus.DELETED),
            entityClass,
        ).awaitSingle()
        log.info("Soft deleted by id = {}", id)
    }

    suspend fun hardDelete(id: ObjectId) {
        log.info("Hard deleting by id = {}", id)
        mongoTemplate.remove(
            Query(Criteria.where(MongoDocument.ID).`is`(id)),
        ).awaitSingle()
        log.info("Hard deleted by id = {}", id)
    }

    suspend fun findById(id: ObjectId): T? {
        log.info("Finding by id = {}", id)
        return mongoTemplate
            .findOne(
                Query(Criteria.where(MongoDocument.ID).`is`(id)),
                entityClass,
            )
            .awaitSingleOrNull()
            .also { log.info("Found by id = {} object = {}", id, it) }
    }

    suspend fun findByIdWithStatus(id: ObjectId, status: DocumentStatus): T? {
        log.info("Finding by id = {} and status = {}", id, status)
        return mongoTemplate
            .findOne(
                Query(
                    Criteria.where(MongoDocument.ID).`is`(id).and(MongoDocument.STATUS).`is`(status),
                ),
                entityClass,
            )
            .awaitSingleOrNull()
            .also { log.info("Found by id = {} and status {} object = {}", id, status, it) }
    }

    suspend fun existsById(id: ObjectId): Boolean {
        log.info("Checking existing by id = {}", id)
        return mongoTemplate
            .exists(
                Query(Criteria.where(MongoDocument.ID).`is`(id)),
                entityClass,
            )
            .awaitSingle()
            .also { log.info("Does exist by id = {} object = {}", id, it) }
    }

    suspend fun existsByIdWithStatus(id: ObjectId, status: DocumentStatus): Boolean {
        log.info("Checking existing by id = {} and status = {}", id, status)
        return mongoTemplate
            .exists(
                Query(
                    Criteria.where(MongoDocument.ID).`is`(id).and(MongoDocument.STATUS).`is`(status),
                ),
                entityClass,
            )
            .awaitSingle()
            .also { log.info("Does exist by id = {} and status = {} object = {}", id, status, it) }
    }
}
