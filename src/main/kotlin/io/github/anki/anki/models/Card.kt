package io.github.anki.anki.models

data class Card(
    var id: Int,
    var parentCollectionId: Int,
    var key: String,
    var value: String)
