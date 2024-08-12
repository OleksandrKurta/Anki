package io.github.anki.anki.service.model

data class Card(
    val id: String? = null,
    val deckId: String,
    val key: String?,
    val value: String?,
)
