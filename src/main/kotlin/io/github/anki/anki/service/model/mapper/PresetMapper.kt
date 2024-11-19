package io.github.anki.anki.service.model.mapper

import io.github.anki.anki.repository.mongodb.document.MongoPreset
import io.github.anki.anki.service.model.Preset
import org.bson.types.ObjectId

fun MongoPreset.toPreset(): Preset {
    return Preset(
        id = this.id.toString(),
        presetOwnerId = this.presetOwnerId.toString(),
        name = this.presetName,
        maxCardsPerDay = this.maxCardsPerDay,
        learningSteps = this.learningSteps,
        failedSteps = this.failedSteps,
    )
}

fun Preset.toMongo(): MongoPreset {
    return MongoPreset(
        presetOwnerId = ObjectId(this.presetOwnerId),
        presetName = this.name,
        maxCardsPerDay = this.maxCardsPerDay,
        learningSteps = this.learningSteps,
        failedSteps = this.failedSteps,
    )
}
