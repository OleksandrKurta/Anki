package io.github.anki.anki.repository.mongodb

import io.github.anki.anki.repository.mongodb.document.DocumentStatus
import io.github.anki.anki.repository.mongodb.document.MongoDocument
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Future

abstract class MongoRepository<T : MongoDocument>(
    protected open var threadPool: ThreadPoolTaskExecutor,
) {
    protected abstract val mongoTemplate: MongoTemplate
    protected abstract val entityClass: Class<T>
    protected abstract val log: Logger

    fun insert(obj: T): Future<T> =
        threadPool.submit<T> {
            log.info("Inserting {}", obj)
            mongoTemplate.insert(obj)
                .also { log.info("Inserted object = {}", it) }
        }

    fun insert(objects: Iterable<T>): Future<List<T>> =
        threadPool.submit<List<T>> {
            log.info("Inserting {}", objects)
            mongoTemplate.insertAll(objects.toList())
                .toList()
                .also { log.info("Inserted object = {}", it) }
        }

    fun save(obj: T): Future<T> =
        threadPool.submit<T> {
            log.info("Saving {}", obj)
            mongoTemplate.save(obj).also { log.info("Saved object = {}", it) }
        }

    fun softDelete(id: ObjectId): Future<*> =
        threadPool.submit {
            log.info("Soft deleting by id = {}", id)
            mongoTemplate.updateFirst(
                Query(Criteria.where(MongoDocument.ID).`is`(id)),
                Update().set(MongoDocument.STATUS, DocumentStatus.DELETED),
                entityClass,
            )
            log.info("Soft deleted by id = {}", id)
        }

    fun hardDelete(id: ObjectId): Future<*> =
        threadPool.submit {
            log.info("Hard deleting by id = {}", id)
            mongoTemplate.remove(
                Query(Criteria.where(MongoDocument.ID).`is`(id)),
            )
            log.info("Hard deleted by id = {}", id)
        }

    fun findById(id: ObjectId): Future<T?> =
        threadPool.submit<T?> {
            log.info("Finding by id = {}", id)
            mongoTemplate.findOne(
                Query(Criteria.where(MongoDocument.ID).`is`(id)),
                entityClass,
            ).also { log.info("Found by id = {} object = {}", id, it) }
        }

    fun findByIdWithStatus(id: ObjectId, status: DocumentStatus): Future<T?> =
        threadPool.submit<T?> {
            log.info("Finding by id = {} and status = {}", id, status)
            mongoTemplate.findOne(
                Query(
                    Criteria.where(MongoDocument.ID).`is`(id).and(MongoDocument.STATUS).`is`(status),
                ),
                entityClass,
            ).also { log.info("Found by id = {} and status {} object = {}", id, status, it) }
        }

    fun existsById(id: ObjectId): Future<Boolean> =
        threadPool.submit<Boolean> {
            log.info("Checking existing by id = {}", id)
            mongoTemplate.exists(
                Query(Criteria.where(MongoDocument.ID).`is`(id)),
                entityClass,
            ).also { log.info("Does exist by id = {} object = {}", id, it) }
        }

    fun existsByIdWithStatus(id: ObjectId, status: DocumentStatus): Future<Boolean> =
        threadPool.submit<Boolean> {
            log.info("Checking existing by id = {} and status = {}", id, status)
            mongoTemplate.exists(
                Query(
                    Criteria.where(MongoDocument.ID).`is`(id).and(MongoDocument.STATUS).`is`(status),
                ),
                entityClass,
            ).also { log.info("Does exist by id = {} and status = {} object = {}", id, status, it) }
        }
}
