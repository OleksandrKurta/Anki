package io.github.anki.anki.controller.dto

import jakarta.validation.constraints.NotBlank


data class NewCardRequest (
    @field:NotBlank(message = "should not be blank")
    val cardKey: String?,

    @field:NotBlank(message = "should not be blank")
    val cardValue: String?,
)

data class CardDtoResponse (
    val id: String,
    val deckId: String,
    val cardKey: String,
    val cardValue: String,
)
