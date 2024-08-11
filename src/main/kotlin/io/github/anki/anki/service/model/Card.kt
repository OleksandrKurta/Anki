package io.github.anki.anki.service.model

data class Card(
    val id: String? = null,
    val deckId: String,
    val cardKey: String?,
    val cardValue: String?,
)
