package io.github.anki.anki.repository.mongodb

import io.github.anki.anki.repository.mongodb.document.DocumentStatus
import io.github.anki.anki.repository.mongodb.document.MongoDeck
import io.github.anki.anki.repository.mongodb.document.MongoDocument
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
class DeckRepository(
    override val mongoTemplate: ReactiveMongoTemplate,
) : MongoRepository<MongoDeck>() {

    override val entityClass = MongoDeck::class.java
    override val log: Logger = LoggerFactory.getLogger(DeckRepository::class.java)

    fun findByUserIdWithStatus(
        userId: ObjectId,
        status: DocumentStatus = DocumentStatus.ACTIVE,
    ): Flux<MongoDeck> =
        mongoTemplate
            .find(
                Query(
                    Criteria.where(MongoDeck.USER_ID).`is`(userId).and(MongoDocument.DOCUMENT_STATUS).`is`(status),
                ),
                entityClass,
            )
            .doFirst { log.info("Finding by userId = {} and status = {}", userId, status) }
            .buffer(CHUNK_SIZE_TO_LOG)
            .doOnNext { log.info("Found by userId = {} and status = {} objects = {}", userId, status, it) }
//            .switchIfEmpty {
//                Flux.defer {
//                    log.info("Nothing was found by userId = {} and status = {}", userId, status)
//                    Flux.empty<MongoDeck>()
//                }
//            }
            .flatMapIterable { list -> list }

    fun findByIdAndUserIdWithStatus(
        id: ObjectId,
        userId: ObjectId,
        status: DocumentStatus = DocumentStatus.ACTIVE,
    ): Mono<MongoDeck> =
        mongoTemplate
            .findOne(
                Query(
                    Criteria
                        .where(MongoDocument.ID)
                        .`is`(id)
                        .and(MongoDeck.USER_ID)
                        .`is`(userId)
                        .and(MongoDocument.DOCUMENT_STATUS)
                        .`is`(status),
                ),
                entityClass,
            )
            .doFirst { log.info("Finding by id = {} userId = {} and status = {}", id, userId, status) }
            .doOnNext {
                log.info(
                    "Found by id = {} and userId = {} and status = {} object = {}",
                    id,
                    userId,
                    status,
                    it,
                )
            }

    fun existsByIdAndUserIdWithStatus(
        id: ObjectId,
        userId: ObjectId,
        status: DocumentStatus,
    ): Mono<Boolean> =
        mongoTemplate.exists(
            Query(
                Criteria
                    .where(MongoDocument.ID)
                    .`is`(id)
                    .and(MongoDeck.USER_ID)
                    .`is`(userId)
                    .and(MongoDocument.DOCUMENT_STATUS)
                    .`is`(status),
            ),
            entityClass,
        )
            .doFirst { log.info("Checking existing by id = {} and userId = {} and status = {}", id, userId, status) }
            .doOnNext {
                log.info(
                    "Does exist by id = {} and userId = {} and status = {} object = {}",
                    id,
                    userId,
                    status,
                    it,
                )
            }

    companion object {
        private const val CHUNK_SIZE_TO_LOG: Int = 10
    }
}
