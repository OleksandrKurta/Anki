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

    fun insert(mongoDecks: Iterable<MongoDeck>): List<MongoDeck> =
        mongoTemplate.insertAll(mongoDecks.toMutableList()).toList()

    fun save(mongoDeck: MongoDeck): MongoDeck =
        mongoTemplate.save(mongoDeck)

    fun findById(id: ObjectId, status: MongoDeck.Status = MongoDeck.Status.ACTIVE): MongoDeck? =
        mongoTemplate.findOne(
            Query(
                Criteria.where(MongoDeck.ID).`is`(id).and(MongoDeck.STATUS).`is`(status),
            ),
            entityClass,
        )

    fun existsById(id: ObjectId, status: MongoDeck.Status = MongoDeck.Status.ACTIVE): Boolean =
        mongoTemplate.exists(
            Query(
                Criteria.where(MongoDeck.ID).`is`(id).and(MongoDeck.STATUS).`is`(status),
            ),
            entityClass,
        )

    fun findByUserId(userId: ObjectId, status: MongoDeck.Status = MongoDeck.Status.ACTIVE): List<MongoDeck> =
        mongoTemplate.find(
            Query(
                Criteria.where(MongoDeck.USER_ID).`is`(userId).and(MongoDeck.STATUS).`is`(status),
            ),
            entityClass,
        )

    fun findByIdAndUserId(
        id: ObjectId,
        userId: ObjectId,
        status: MongoDeck.Status = MongoDeck.Status.ACTIVE,
    ): MongoDeck? =
        mongoTemplate.findOne(
            Query(
                Criteria
                    .where(MongoDeck.ID)
                    .`is`(id)
                    .and(MongoDeck.USER_ID)
                    .`is`(userId)
                    .and(MongoDeck.STATUS)
                    .`is`(status),
            ),
            entityClass,
        )

    fun deleteById(id: ObjectId) {
        mongoTemplate.updateFirst(
            Query(Criteria.where(MongoDeck.ID).`is`(id)),
            Update().set(MongoDeck.STATUS, MongoDeck.Status.DELETED),
            entityClass,
        )
    }
}
