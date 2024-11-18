package io.github.anki.anki.repository.mongodb

import io.github.anki.anki.repository.mongodb.document.DocumentStatus
import io.github.anki.anki.repository.mongodb.document.MongoCard
import io.github.anki.anki.repository.mongodb.document.MongoDeckEvent
import io.github.anki.anki.repository.mongodb.document.MongoDocument
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
class DeckEventRepository(
    override val mongoTemplate: ReactiveMongoTemplate,
) : MongoRepository<MongoDeckEvent>() {

    override val entityClass = MongoDeckEvent::class.java
    override val log: Logger = LoggerFactory.getLogger(DeckEventRepository::class.java)

    fun findByDeckIdWithStatus(
        deckId: ObjectId,
        status: DocumentStatus = DocumentStatus.ACTIVE,
        limit: Int = 50,
        offset: Int = 0,
    ): Flux<MongoDeckEvent> =
        mongoTemplate.find(
            Query(
                Criteria.where(MongoCard.DECK_ID).`is`(deckId).and(MongoDocument.DOCUMENT_STATUS).`is`(status),
            ).limit(limit).skip(offset.toLong()),
            entityClass,
        )
            .doFirst { log.info("Finding by deckId = {} and status = {}", deckId, status) }
            .buffer(CHUNK_SIZE_TO_LOG)
            .doOnNext { log.info("Found by deckId = {} and status = {} objects = {}", deckId, status, it) }
            .flatMapIterable { it }

    companion object {
        private const val CHUNK_SIZE_TO_LOG: Int = 50
    }
}
