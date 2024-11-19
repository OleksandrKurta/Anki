package io.github.anki.anki.repository.mongodb

import io.github.anki.anki.configuration.AppConfiguration
import io.github.anki.anki.repository.mongodb.document.DocumentStatus
import io.github.anki.anki.repository.mongodb.document.MongoDocument
import io.github.anki.anki.repository.mongodb.document.MongoPreset
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.task.AsyncTaskExecutor
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import java.util.concurrent.CompletableFuture

@Repository
class PresetRepository(
    override val mongoTemplate: MongoTemplate,
    @Qualifier(AppConfiguration.MONGO_THREAD_POOL_QUALIFIER) override val threadPool: AsyncTaskExecutor,
) : MongoRepository<MongoPreset>(threadPool) {

    override val entityClass = MongoPreset::class.java
    override val log: Logger = LoggerFactory.getLogger(PresetRepository::class.java)

    fun findUserPresets(
        userId: ObjectId,
        status: DocumentStatus = DocumentStatus.ACTIVE,
    ): CompletableFuture<List<MongoPreset>> =
        threadPool.submitCompletable<List<MongoPreset>> {
            log.info("Finding by userId = {} and status = {}", userId, status)
            mongoTemplate.find(
                Query(
                    Criteria.where(
                        MongoPreset.PRESET_OWNER_ID,
                    ).`is`(userId).and(MongoDocument.DOCUMENT_STATUS).`is`(status),
                ),
                entityClass,
            ).also { log.info("Found by userId = {} and status = {} object = {}", userId, status, it) }
        }

    fun findPresetById(
        presetId: ObjectId,
        status: DocumentStatus = DocumentStatus.ACTIVE,
    ): CompletableFuture<MongoPreset> =
        threadPool.submitCompletable<MongoPreset> {
            log.info("Finding by id = {} and status = {}", presetId, status)
            mongoTemplate.findOne(
                Query(
                    Criteria.where(MongoDocument.ID).`is`(presetId).and(MongoDocument.DOCUMENT_STATUS).`is`(status),
                ),
                entityClass,
            ).also { log.info("Found by id = {} and status = {} object = {}", presetId, status, it) }
        }

    fun countUserPresets(
        userId: String?,
        status: DocumentStatus = DocumentStatus.ACTIVE,
    ): CompletableFuture<Long> =
        threadPool.submitCompletable<Long> {
            log.info("Counting by userId = {} and status = {}", userId, status)
            mongoTemplate.count(
                Query(
                    Criteria.where(
                        MongoPreset.PRESET_OWNER_ID,
                    ).`is`(userId).and(MongoDocument.DOCUMENT_STATUS).`is`(status),
                ),
                entityClass,
            ).also { log.info("Counting by userId = {} and status = {} object = {}", userId, status, it) }
        }

    fun findDefaultByUserId(
        userId: ObjectId,
        defaultPreset: Boolean = true,
    ): CompletableFuture<MongoPreset> =
        threadPool.submitCompletable<MongoPreset> {
            log.info("Finding by userId = {} and defaultStatus = {}", userId, defaultPreset)
            mongoTemplate.findOne(
                Query(
                    Criteria.where(MongoPreset.PRESET_OWNER_ID).`is`(userId).and(MongoPreset.DEFAULT).`is`(defaultPreset),
                ),
                entityClass,
            ).also { log.info("Found by userId = {} and defaultStatus = {} object = {}", userId, defaultPreset, it) }
        }
}
