package io.github.anki.anki.service.model.mapper

import io.github.anki.anki.repository.mongodb.document.MongoRateLog
import org.bson.types.ObjectId
import service.model.CardLearningEntity
import service.model.CardRate
import service.model.RateEnum
import service.model.RateLog

fun MongoRateLog.toRate(): RateLog {
    return RateLog(
        this.id.toString(),
        CardRate(RateEnum.valueOf(this.learningRate)),
        this.deckId.toString(),
        this.learningItemId.toString(),
        this.userId.toString(),
    )
}

fun RateLog.toMongo(): MongoRateLog {
    return MongoRateLog(
        id = this.id?.let { ObjectId(it) },
        learningRate = this.learningRate.getRate().name,
        learningItemId = ObjectId(this.learningItemId),
        userId = ObjectId(this.userId),
        deckId = ObjectId(this.deckId),
    )
}

fun RateLog.toCardEntity(): CardLearningEntity {
    return CardLearningEntity.buildFromRateLog(
        this.learningItemId,
        this.id,
        this.deckId,
    )
}
