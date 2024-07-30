package io.github.anki.anki.controller.dto

import jakarta.validation.constraints.NotBlank


data class NewCardRequest (
    @field:NotBlank(message = "should not be blank")
    var deckId: String?,

    @field:NotBlank(message = "should not be blank")
    var cardKey: String?,

    @field:NotBlank(message = "should not be blank")
    var cardValue: String?,
)

data class CardDtoResponse (
    var id: String?,
    var deckId: String?,
    var cardKey: String?,
    var cardValue: String?,
)
