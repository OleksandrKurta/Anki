package io.github.anki.anki.repository.mongodb

import io.github.anki.anki.repository.mongodb.document.MongoDeck
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository

@Repository
class DeckRepository(
    private val mongoTemplate: MongoTemplate,
) {
    private val entityClass = MongoDeck::class.java

    fun insert(mongoDeck: MongoDeck): MongoDeck =
        mongoTemplate.insert(mongoDeck)

    fun insert(mongoDecks: List<MongoDeck>): List<MongoDeck> =
        mongoTemplate.insertAll(mongoDecks.toMutableList()).toList()

    fun save(mongoDeck: MongoDeck): MongoDeck =
        mongoTemplate.save(mongoDeck)

    fun findById(id: ObjectId): MongoDeck? =
        mongoTemplate.findOne(
            Query(
                Criteria.where(MongoDeck.ID).`is`(id).and(MongoDeck.STATUS).`is`(MongoDeck.Status.ACTIVE),
            ),
            entityClass,
        )

    fun existsById(id: ObjectId): Boolean =
        mongoTemplate.exists(
            Query(
                Criteria.where(MongoDeck.ID).`is`(id).and(MongoDeck.STATUS).`is`(MongoDeck.Status.ACTIVE),
            ),
            entityClass,
        )

    fun findByUserId(userId: ObjectId): List<MongoDeck> =
        mongoTemplate.find(
            Query(
                Criteria.where(MongoDeck.USER_ID).`is`(userId).and(MongoDeck.STATUS).`is`(MongoDeck.Status.ACTIVE),
            ),
            entityClass,
        )

    fun findByIdAndUserId(id: ObjectId, userId: ObjectId): MongoDeck? =
        mongoTemplate.findOne(
            Query(
                Criteria
                    .where(MongoDeck.ID)
                    .`is`(id)
                    .and(MongoDeck.USER_ID)
                    .`is`(userId)
                    .and(MongoDeck.STATUS)
                    .`is`(MongoDeck.Status.ACTIVE),
            ),
            entityClass,
        )

    fun deleteById(id: ObjectId) =
        mongoTemplate.updateFirst(
            Query(Criteria.where(MongoDeck.ID).`is`(id)),
            Update().set(MongoDeck.STATUS, MongoDeck.Status.DELETED),
            entityClass,
        )
}
