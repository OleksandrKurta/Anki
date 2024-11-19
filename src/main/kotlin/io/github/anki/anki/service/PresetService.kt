package io.github.anki.anki.service

import io.github.anki.anki.repository.mongodb.PresetRepository
import io.github.anki.anki.service.exceptions.PresetCreationLimitException
import io.github.anki.anki.service.exceptions.PresetNotExistException
import io.github.anki.anki.service.model.Preset
import io.github.anki.anki.service.model.mapper.toMongo
import io.github.anki.anki.service.model.mapper.toPreset
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class PresetService(
    private val presetRepository: PresetRepository,
) {

    @Value("\${anki.app.presetsLimit}")
    private val presetsLimit: Double = 0.0

    @Autowired
    private val preset: Preset? = null

    fun getDefaultPresetObj(userId: String): Preset {
        if (this.preset != null) {
            return Preset(name = this.preset.name,
                maxCardsPerDay = this.preset.maxCardsPerDay,
                presetOwnerId = userId,
                learningSteps = this.preset.learningSteps,
                failedSteps = this.preset.failedSteps)
        }
        throw UninitializedPropertyAccessException("Default preset get before bean initialization")
    }

    fun getPresets(userId: String): List<Preset> {
        return presetRepository
            .findUserPresets(ObjectId(userId))
            .get()
            .map { it.toPreset() }
    }

    fun createNewPreset(preset: Preset): Preset {
        if (presetRepository.countUserPresets(userId = preset.presetOwnerId).get() > presetsLimit) {
            throw PresetCreationLimitException.fromUserId(preset.presetOwnerId)
        }
        return presetRepository
            .insert(preset.toMongo())
            .get()
            .toPreset()
    }

    fun updatePreset(preset: Preset): Preset {
        val presetId = preset.id ?: throw IllegalArgumentException("Preset id can not be null")
        val mongoPreset =
            presetRepository.findPresetById(
                presetId = ObjectId(presetId),
            ).get() ?: throw PresetNotExistException.fromPresetId(preset.id!!)
        val updatedMongoPreset = preset.toMongo()
        if (mongoPreset == updatedMongoPreset) {
            return mongoPreset.toPreset()
        }
        return presetRepository
            .save(updatedMongoPreset)
            .get()
            .toPreset()
    }

    fun deletePreset(presetId: String, presetOwnerId: String) {
        if (!presetRepository.existsByIdAndUserIdWithStatus(ObjectId(presetId), ObjectId(presetOwnerId)).get()) {
            throw PresetNotExistException.fromPresetId(presetId)
        }
        presetRepository.softDelete(ObjectId(presetId))
    }

    fun getDefaultPresetOrCreateNew(userId: String): Preset {
        if (presetRepository.countUserPresets(userId).get() > 0) {
            return presetRepository.findDefaultByUserId(userId = ObjectId(userId), defaultPreset = true).get().toPreset()
        }
        val defaultPreset = this.getDefaultPresetObj(userId)
        val mongoPreset = presetRepository.insert(defaultPreset.toMongo()).get()
        defaultPreset.id = mongoPreset.id.toString()
        return defaultPreset

    }
}
