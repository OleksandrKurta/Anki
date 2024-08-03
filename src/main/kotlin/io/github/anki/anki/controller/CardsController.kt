package io.github.anki.anki.controller

import io.github.anki.anki.controller.dto.CardDtoResponse
import io.github.anki.anki.controller.dto.NewCardRequest
import io.github.anki.anki.controller.dto.mapper.toCard
import io.github.anki.anki.controller.dto.mapper.toDto
import io.github.anki.anki.service.CardsService
import io.github.anki.anki.service.DeckService
import io.github.anki.anki.service.model.Card
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus

@RestController
@RequestMapping("/api/v1/decks/{deckId}/cards")
class CardsController(
    private val cardService: CardsService,
    private val deckService: DeckService,
) {
    private val requestUserId = "66a11305dc669eefd22b5f3a"

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createCard(@Valid @RequestBody request: NewCardRequest, @PathVariable deckId: String): CardDtoResponse {
        deckService.getDeckByIdAndUserId(deckId, requestUserId)
        val newCard: Card = cardService.createNewCard(request.toCard(deckId))
        return newCard.toDto()
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getAllCardsFromDeck(@PathVariable deckId: String): List<CardDtoResponse> {
        deckService.getDeckByIdAndUserId(deckId, requestUserId)
        return cardService.getAllCardsFromDeck(deckId).map { it.toDto() }
    }



    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCard(@PathVariable id: String) {
        cardService.deleteCard(id)
    }
}
