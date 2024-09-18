package io.github.anki.anki.repository.mongodb

import io.github.anki.anki.repository.mongodb.document.DocumentStatus
import io.github.anki.anki.repository.mongodb.document.MongoDocument
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.springframework.core.task.AsyncTaskExecutor
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import java.util.concurrent.CompletableFuture

abstract class MongoRepository<T : MongoDocument>(
    protected open val threadPool: AsyncTaskExecutor,
) {
    protected abstract val mongoTemplate: MongoTemplate
    protected abstract val entityClass: Class<T>
    protected abstract val log: Logger

    fun insert(obj: T): CompletableFuture<T> =
        threadPool.submitCompletable<T> {
            log.info("Inserting {}", obj)
            mongoTemplate.insert(obj)
                .also { log.info("Inserted object = {}", it) }
        }

    fun insert(objects: Iterable<T>): CompletableFuture<List<T>> =
        threadPool.submitCompletable<List<T>> {
            log.info("Inserting {}", objects)
            mongoTemplate.insertAll(objects.toList())
                .toList()
                .also { log.info("Inserted object = {}", it) }
        }

    fun save(obj: T): CompletableFuture<T> =
        threadPool.submitCompletable<T> {
            log.info("Saving {}", obj)
            mongoTemplate.save(obj).also { log.info("Saved object = {}", it) }
        }

    fun softDelete(id: ObjectId): CompletableFuture<Void> =
        threadPool.submitCompletable {
            log.info("Soft deleting by id = {}", id)
            mongoTemplate.updateFirst(
                Query(Criteria.where(MongoDocument.ID).`is`(id)),
                Update().set(MongoDocument.DOCUMENT_STATUS, DocumentStatus.DELETED),
                entityClass,
            )
            log.info("Soft deleted by id = {}", id)
        }

    fun findById(id: ObjectId): CompletableFuture<T?> =
        threadPool.submitCompletable<T?> {
            log.info("Finding by id = {}", id)
            mongoTemplate.findOne(
                Query(Criteria.where(MongoDocument.ID).`is`(id)),
                entityClass,
            ).also { log.info("Found by id = {} object = {}", id, it) }
        }

    fun findByIdWithStatus(id: ObjectId, status: DocumentStatus): CompletableFuture<T?> =
        threadPool.submitCompletable<T?> {
            log.info("Finding by id = {} and status = {}", id, status)
            mongoTemplate.findOne(
                Query(
                    Criteria.where(MongoDocument.ID).`is`(id).and(MongoDocument.DOCUMENT_STATUS).`is`(status),
                ),
                entityClass,
            ).also { log.info("Found by id = {} and status {} object = {}", id, status, it) }
        }

    fun existsById(id: ObjectId): CompletableFuture<Boolean> =
        threadPool.submitCompletable<Boolean> {
            log.info("Checking existing by id = {}", id)
            mongoTemplate.exists(
                Query(Criteria.where(MongoDocument.ID).`is`(id)),
                entityClass,
            ).also { log.info("Does exist by id = {} object = {}", id, it) }
        }

    fun existsByIdWithStatus(id: ObjectId, status: DocumentStatus): CompletableFuture<Boolean> =
        threadPool.submitCompletable<Boolean> {
            log.info("Checking existing by id = {} and status = {}", id, status)
            mongoTemplate.exists(
                Query(
                    Criteria.where(MongoDocument.ID).`is`(id).and(MongoDocument.DOCUMENT_STATUS).`is`(status),
                ),
                entityClass,
            ).also { log.info("Does exist by id = {} and status = {} object = {}", id, status, it) }
        }
}
