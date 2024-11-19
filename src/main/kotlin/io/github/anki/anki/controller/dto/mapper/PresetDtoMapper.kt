package io.github.anki.anki.controller.dto.mapper

import io.github.anki.anki.controller.dto.NewPresetDto
import io.github.anki.anki.controller.dto.PatchPresetDto
import io.github.anki.anki.controller.dto.PresetDtoResponse
import io.github.anki.anki.service.model.Preset

fun NewPresetDto.toPreset(ownerId: String) =
    Preset(
        name = this.name,
        presetOwnerId = ownerId,
        maxCardsPerDay = this.maxCardsPerDay,
        learningSteps = this.learningSteps,
        failedSteps = this.failedSteps,
    )

fun PatchPresetDto.toPreset(presetId: String, ownerId: String) =
    Preset(
        id = presetId,
        name = this.name,
        presetOwnerId = ownerId,
        maxCardsPerDay = this.maxCardsPerDay,
        learningSteps = this.learningSteps,
        failedSteps = this.failedSteps,
    )

fun Preset.toDto(): PresetDtoResponse {
    return PresetDtoResponse(
        this.id.toString(),
        this.name.toString(),
        this.maxCardsPerDay,
        this.learningSteps,
        this.failedSteps,
    )
}
