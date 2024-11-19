package io.github.anki.anki.controller.dto

import jakarta.validation.constraints.NotBlank


data class NewCardRequest(
    @field:NotBlank
    val key: String?,
    @field:NotBlank
    val value: String?,
)

data class PatchCardRequest(
    val key: Any? = null,
    val value: Any? = null,
)

data class EntityDtoResponse(
    val id: String,
    val deckId: String,
    val key: Any?,
    val value: Any?,
    val lastRateId: String? = null,
)
