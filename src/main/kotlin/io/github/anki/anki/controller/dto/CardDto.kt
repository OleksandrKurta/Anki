package io.github.anki.anki.controller.dto

import jakarta.validation.constraints.NotBlank

data class NewCardRequest(
    @field:NotBlank
    val key: String?,
    @field:NotBlank
    val value: String?,
)

data class PatchCardRequest(
    val key: String? = null,
    val value: String? = null,
)

data class CardDtoResponse(
    val id: String,
    val deckId: String,
    val key: String,
    val value: String,
)
