package io.github.anki.anki.repository.mongodb

import io.github.anki.anki.repository.mongodb.document.MongoCard
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository

@Repository
class CardRepository(
    private val mongoTemplate: MongoTemplate,
) {
    private val entityClass = MongoCard::class.java

    fun insert(mongoCard: MongoCard): MongoCard =
        mongoTemplate.insert(mongoCard)

    fun insert(mongoCards: List<MongoCard>): List<MongoCard> =
        mongoTemplate.insertAll(mongoCards.toMutableList()).toList()

    fun findByDeckId(deckId: ObjectId): List<MongoCard> =
        mongoTemplate.find(
            Query(
                Criteria.where(MongoCard.DECK_ID).`is`(deckId).and(MongoCard.STATUS).`is`(MongoCard.Status.ACTIVE),
            ),
            entityClass,
        )

    fun save(mongoCard: MongoCard): MongoCard =
        mongoTemplate.save(mongoCard)

    fun deleteById(id: ObjectId) =
        mongoTemplate.updateFirst(
            Query(Criteria.where(MongoCard.ID).`is`(id)),
            Update().set(MongoCard.STATUS, MongoCard.Status.DELETED),
            entityClass,
        )

    fun findById(id: ObjectId): MongoCard? =
        mongoTemplate.findOne(
            Query(
                Criteria.where(MongoCard.ID).`is`(id).and(MongoCard.STATUS).`is`(MongoCard.Status.ACTIVE),
            ),
            entityClass,
        )

    fun existsById(id: ObjectId): Boolean =
        mongoTemplate.exists(
            Query(
                Criteria.where(MongoCard.ID).`is`(id).and(MongoCard.STATUS).`is`(MongoCard.Status.ACTIVE),
            ),
            entityClass,
        )

    fun deleteByDeckId(deckId: ObjectId) =
        mongoTemplate.updateMulti(
            Query(Criteria.where(MongoCard.DECK_ID).`is`(deckId)),
            Update().set(MongoCard.STATUS, MongoCard.Status.DELETED),
            entityClass,
        )
}
