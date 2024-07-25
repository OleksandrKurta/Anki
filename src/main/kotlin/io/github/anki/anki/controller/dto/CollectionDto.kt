package io.github.anki.anki.controller.dto

data class NewDeckRequest (
    var userId: String,
    var name: String,
    var description: String?,
)

data class DeckDtoResponse (
    var id: String?,
    var userId: String,
    var name: String,
    var description: String?,
)
