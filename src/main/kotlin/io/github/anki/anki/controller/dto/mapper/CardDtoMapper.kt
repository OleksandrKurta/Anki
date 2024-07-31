package io.github.anki.anki.controller.dto.mapper

import io.github.anki.anki.controller.dto.CardDtoResponse
import io.github.anki.anki.controller.dto.NewCardRequest
import io.github.anki.anki.service.model.Card

fun NewCardRequest.toCard(): Card =
    Card(
        deckId = this.deckId!!,
        cardKey = this.cardKey!!,
        cardValue = this.cardValue!!,
    )

fun Card.toDto(): CardDtoResponse =
    CardDtoResponse(
        id = this.id!!,
        deckId = this.deckId!!,
        cardKey = this.cardKey!!,
        cardValue = this.cardValue!!,
    )
