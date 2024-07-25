package io.github.anki.anki.service.model

data class Deck(
    var id: String? = null,
    var userId: String,
    var name: String,
    var description: String?,
)
