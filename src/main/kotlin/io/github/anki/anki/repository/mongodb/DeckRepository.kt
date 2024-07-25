package io.github.anki.anki.repository.mongodb

import io.github.anki.anki.repository.mongodb.document.MongoDeck
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository


@Repository
interface DeckRepository : MongoRepository<MongoDeck, ObjectId>
