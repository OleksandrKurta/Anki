package io.github.anki.anki.repository.mongodb

import io.github.anki.anki.repository.mongodb.document.MongoCard
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface CardRepository: MongoRepository<MongoCard, ObjectId> {

    fun deleteById(id: String) {
        deleteById(ObjectId(id))
    }
}
