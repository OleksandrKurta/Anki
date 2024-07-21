package io.github.anki.anki.repository.mongodb

import io.github.anki.anki.repository.mongodb.model.MongoCard
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface CardRepository: MongoRepository<MongoCard, String> {
    fun findByParentCollectionId(parentCollectionId: String): List<MongoCard>
    fun deleteById(id: ObjectId): MongoCard
}
