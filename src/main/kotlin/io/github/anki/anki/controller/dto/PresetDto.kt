package io.github.anki.anki.controller.dto

data class NewPresetDto(
    val name: String,
    val maxCardsPerDay: Int,
    val learningSteps: ArrayList<String>,
    val failedSteps: ArrayList<String>,
)

data class PatchPresetDto(
    val name: String,
    val maxCardsPerDay: Int,
    val learningSteps: ArrayList<String>,
    val failedSteps: ArrayList<String>,
)

data class PresetDtoResponse(
    val id: String,
    val name: String,
    val maxCardsPerDay: Int,
    val learningSteps: ArrayList<String>,
    val failedSteps: ArrayList<String>,
)
