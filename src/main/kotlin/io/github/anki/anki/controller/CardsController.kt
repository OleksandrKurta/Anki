package io.github.anki.anki.controller

import io.github.anki.anki.controller.dto.CardDtoResponse
import io.github.anki.anki.controller.dto.NewCardRequest
import io.github.anki.anki.controller.dto.PaginationDto
import io.github.anki.anki.controller.dto.PatchCardRequest
import io.github.anki.anki.controller.dto.mapper.toCard
import io.github.anki.anki.controller.dto.mapper.toDto
import io.github.anki.anki.service.CardsService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/decks/{deckId}/cards")
class CardsController(
    private val cardService: CardsService,
) {
    private val requestUserId = "66a11305dc669eefd22b5f3a"

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createCard(
        @Valid
        @RequestBody
        request: NewCardRequest,
        @PathVariable
        deckId: String,
    ): CardDtoResponse =
        cardService.createNewCard(
            userId = requestUserId,
            request.toCard(deckId),
        ).toDto()

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getAllCardsFromDeck(
        @PathVariable deckId: String,
        @RequestBody pagination: PaginationDto,
    ): List<CardDtoResponse> =
        cardService.findCardsByDeckWithPagination(
            deckId = deckId,
            userId = requestUserId,
            limit = pagination.limit,
            offset = pagination.offset,
        ).map { it.toDto() }

    @PatchMapping("/{cardId}")
    @ResponseStatus(HttpStatus.OK)
    fun patchCard(
        @PathVariable deckId: String,
        @PathVariable cardId: String,
        @RequestBody request: PatchCardRequest,
    ): CardDtoResponse =
        cardService.updateCard(
            userId = requestUserId,
            request.toCard(cardId, deckId),
        ).toDto()

    @DeleteMapping("/{cardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCard(@PathVariable deckId: String, @PathVariable cardId: String) {
        cardService.deleteCard(
            deckId = deckId,
            userId = requestUserId,
            cardId = cardId,
        )
    }
}
