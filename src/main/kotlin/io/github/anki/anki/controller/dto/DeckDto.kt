package io.github.anki.anki.controller.dto

import jakarta.validation.constraints.NotBlank

data class NewDeckRequest(
    @field:NotBlank
    val name: String?,
    val description: String?,
)

data class PatchDeckRequest(
    val name: String? = null,
    val description: String? = null,
)

data class DeckDtoResponse(
    val id: String,
    val userId: String,
    val name: String,
    val description: String?,
)
