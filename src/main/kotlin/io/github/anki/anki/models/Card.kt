package io.github.anki.anki.models

data class CardsCollection(
    var id: Int,
    var name: String,
    var description: String,
)

data class Card(
    var id: Int,
    var parentCollectionId: Int,
    var key: String,
    var value: String)
