package io.github.anki.anki.controller.dto

data class NewCardRequest (
    var collectionId: String,
    var cardKey: String,
    var cardValue: String,
)

data class CardDtoResponse (
    var id: String?,
    var collectionId: String?,
    var cardKey: String?,
    var cardValue: String?,
)
