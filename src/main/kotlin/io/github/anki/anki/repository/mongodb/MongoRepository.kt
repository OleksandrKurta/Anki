package io.github.anki.anki.repository.mongodb

import io.github.anki.anki.repository.mongodb.document.DocumentStatus
import io.github.anki.anki.repository.mongodb.document.MongoDocument
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.springframework.core.task.AsyncTaskExecutor
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

abstract class MongoRepository<T : MongoDocument>(
    protected open val threadPool: AsyncTaskExecutor,
) {
    protected abstract val mongoTemplate: ReactiveMongoTemplate
    protected abstract val entityClass: Class<T>
    protected abstract val log: Logger

    fun insert(obj: T): Mono<T> =
        mongoTemplate
            .insert(obj)
            .doFirst { log.info("Inserting {}", obj) }
            .doOnNext { log.info("Inserted object = {}", it) }

    fun insert(objects: Collection<T>): Flux<T> =
        mongoTemplate
            .insertAll(objects.toList())
            .doFirst { log.info("Inserting {}", objects) }
            .buffer(objects.size)
            .doOnNext { log.info("Inserted objects = {}", it) }
            .flatMapIterable { list -> list }

    fun save(obj: T): Mono<T> =
        mongoTemplate
            .save(obj)
            .doFirst { log.info("Saving {}", obj) }
            .doOnNext { log.info("Saved object = {}", it) }

    fun softDelete(id: ObjectId): Mono<Void> =
        mongoTemplate
            .updateFirst(
                Query(Criteria.where(MongoDocument.ID).`is`(id)),
                Update().set(MongoDocument.DOCUMENT_STATUS, DocumentStatus.DELETED),
                entityClass,
            )
            .doFirst { log.info("Soft deleting by id = {}", id) }
            .doOnNext { log.info("Soft deleted by id = {}", id) }
            .then()

    fun findById(id: ObjectId): Mono<T> =
        mongoTemplate
            .findOne(
                Query(Criteria.where(MongoDocument.ID).`is`(id)),
                entityClass,
            )
            .doFirst { log.info("Finding by id = {}", id) }
            .doOnNext { obj -> log.info("Found by id = {} object = {}", id, obj) }

    fun findByIdWithStatus(id: ObjectId, status: DocumentStatus): Mono<T> =
        mongoTemplate.findOne(
            Query(
                Criteria.where(MongoDocument.ID).`is`(id).and(MongoDocument.DOCUMENT_STATUS).`is`(status),
            ),
            entityClass,
        )
            .doFirst { log.info("Finding by id = {} and status = {}", id, status) }
            .doOnNext { obj -> log.info("Found by id = {} and status {} object = {}", id, status, obj) }

    fun existsById(id: ObjectId): Mono<Boolean> =
        mongoTemplate
            .exists(
                Query(Criteria.where(MongoDocument.ID).`is`(id)),
                entityClass,
            )
            .doFirst { log.info("Checking existing by id = {}", id) }
            .doOnNext { exists -> log.info("Does exist by id = {} object = {}", id, exists) }

    fun existsByIdWithStatus(id: ObjectId, status: DocumentStatus): Mono<Boolean> =
        mongoTemplate
            .exists(
                Query(
                    Criteria.where(MongoDocument.ID).`is`(id).and(MongoDocument.DOCUMENT_STATUS).`is`(status),
                ),
                entityClass,
            )
            .doFirst { log.info("Checking existing by id = {} and status = {}", id, status) }
            .doOnNext { exists -> log.info("Does exist by id = {} and status = {} object = {}", id, status, exists) }
}
