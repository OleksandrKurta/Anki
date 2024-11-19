package io.github.anki.anki.service.model.mapper

import io.github.anki.anki.repository.mongodb.document.MongoCard
import service.model.LearnItem
import org.bson.types.ObjectId
import service.model.CardLearningEntity
import java.time.Instant

fun CardLearningEntity.toMongo(): MongoCard {
     if (this.rateId == null) {
         return MongoCard(
             id = this.id?.let { ObjectId(it) },
             deckId = ObjectId(this.deckId),
             key = this.hint?.item.toString(),
             createdAt = Instant.now(),
             modifiedAt = Instant.now(),
             value = this.answer?.item.toString(),
             lastRateId = null)
     }
    else {
        return MongoCard(
            id = this.id?.let { ObjectId(it) },
            deckId = ObjectId(this.deckId),
            key = this.hint?.item.toString(),
            createdAt = Instant.now(),
            modifiedAt = Instant.now(),
            value = this.answer?.item.toString(),
            lastRateId = ObjectId(this.rateId))
     }
}


fun MongoCard.toCardEntity(): CardLearningEntity =
    CardLearningEntity(
        this.id.toString(),
        this.lastRateId.toString(),
        this.deckId.toString(),
        LearnItem(this.key),
        LearnItem(this.value),
        this.createdAt,
    )
