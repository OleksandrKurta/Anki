package io.github.anki.anki.controller.dto.mapper

import io.github.anki.anki.controller.dto.DeckDtoResponse
import io.github.anki.anki.controller.dto.NewDeckRequest
import io.github.anki.anki.controller.dto.PatchDeckRequest
import io.github.anki.anki.service.model.Deck

fun NewDeckRequest.toDeck(userId: String): Deck =
    Deck(
        userId = userId,
        name = this.name,
        description = this.description,
    )

fun PatchDeckRequest.toDeck(deckId: String, userId: String): Deck =
    Deck(
        id = deckId,
        userId = userId,
        name = this.name,
        description = this.description,
    )

fun Deck.toDto(): DeckDtoResponse =
    DeckDtoResponse(
        id = this.id ?: throw IllegalArgumentException("Deck id can not be null"),
        userId = this.userId,
        name = this.name ?: throw IllegalArgumentException("Deck name can not be null"),
        description = this.description,
    )
