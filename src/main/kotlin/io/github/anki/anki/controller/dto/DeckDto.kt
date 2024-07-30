package io.github.anki.anki.controller.dto

import jakarta.validation.constraints.NotBlank

data class NewDeckRequest (
    @field:NotBlank(message = "should not be blank")
    var name: String?,

    var description: String?,
) {
    lateinit var userId: String
}

data class DeckDtoResponse (
    var id: String?,
    var userId: String?,
    var name: String?,
    var description: String?,
)
