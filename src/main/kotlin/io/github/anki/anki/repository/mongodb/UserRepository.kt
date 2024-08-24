package io.github.anki.anki.repository.mongodb

import io.github.anki.anki.repository.mongodb.document.DocumentStatus
import io.github.anki.anki.repository.mongodb.document.MongoDocument
import io.github.anki.anki.repository.mongodb.document.MongoUser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

@Repository
class UserRepository(
    override val mongoTemplate: MongoTemplate,
) : MongoRepository<MongoUser>() {
    override val entityClass = MongoUser::class.java
    override val log: Logger = LoggerFactory.getLogger(UserRepository::class.java)

    fun findByUserName(userName: String, status: DocumentStatus = DocumentStatus.ACTIVE): MongoUser? {
        log.info("Finding by userName = {} with status = {}", userName, status)
        return mongoTemplate.findOne(
            Query(
                Criteria
                    .where(MongoUser.USER_NAME)
                    .`is`(userName)
                    .and(MongoDocument.DOCUMENT_STATUS)
                    .`is`(status),
            ),
            entityClass,
        ).also { log.info("Found by userName = {} and status = {} object = {}", userName, status, it) }
    }

    fun existsByUserName(userName: String?, status: DocumentStatus = DocumentStatus.ACTIVE): Boolean {
        log.info("Check exists by userName = {} with status = {}", userName, status)
        return mongoTemplate.exists(
            Query(
                Criteria
                    .where(MongoUser.USER_NAME)
                    .`is`(userName)
                    .and(MongoDocument.DOCUMENT_STATUS)
                    .`is`(status),
            ),
            entityClass,
        ).also { log.info("Check exists userName = {} and status = {} object = {}", userName, status, it) }
    }
}
