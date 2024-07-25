package io.github.anki.anki.service.model

data class Card(
    var id: String? = null,
    var collectionId: String?,
    var cardKey: String?,
    var cardValue: String?,
)
