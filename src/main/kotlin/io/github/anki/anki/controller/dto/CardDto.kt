package io.github.anki.anki.controller.dto

data class NewCardRequest(
    var deckId: String,
    var cardKey: String,
    var cardValue: String,
)

data class CardDtoResponse(
    var id: String?,
    var deckId: String?,
    var cardKey: String?,
    var cardValue: String?,
)
