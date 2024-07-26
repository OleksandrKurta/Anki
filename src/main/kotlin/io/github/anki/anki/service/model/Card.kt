package io.github.anki.anki.service.model

data class Card(
    var id: String? = null,
    var deckId: String?,
    var cardKey: String?,
    var cardValue: String?,
)
