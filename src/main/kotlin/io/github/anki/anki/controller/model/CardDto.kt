package io.github.anki.anki.controller.model


class CardDto (
    var id: String? = null,
    var parentCollectionId: String?,
    var cardKey: String?,
    var cardValue: String?,
)

class NewCardDto (
    var parentCollectionId: String?,
    var cardKey: String?,
    var cardValue: String?,
)
