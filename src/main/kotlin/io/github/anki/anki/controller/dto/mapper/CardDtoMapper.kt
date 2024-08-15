package io.github.anki.anki.controller.dto.mapper

import io.github.anki.anki.controller.dto.CardDtoResponse
import io.github.anki.anki.controller.dto.NewCardRequest
import io.github.anki.anki.controller.dto.PatchCardRequest
import io.github.anki.anki.service.model.Card

fun NewCardRequest.toCard(deckId: String): Card =
    Card(
        deckId = deckId,
        key = this.key,
        value = this.value,
    )

fun PatchCardRequest.toCard(cardId: String, deckId: String): Card =
    Card(
        id = cardId,
        deckId = deckId,
        key = this.key,
        value = this.value,
    )

fun Card.toDto(): CardDtoResponse =
    CardDtoResponse(
        id = this.id ?: throw IllegalArgumentException("Card id can not be null"),
        deckId = this.deckId,
        key = this.key ?: throw IllegalArgumentException("Card key can not be null"),
        value = this.value ?: throw IllegalArgumentException("Card value can not be null"),
    )
