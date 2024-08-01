package io.github.anki.anki.controller

import io.github.anki.anki.controller.dto.DeckDtoResponse
import io.github.anki.anki.controller.dto.NewDeckRequest
import io.github.anki.anki.controller.dto.PatchDeckRequest
import io.github.anki.anki.controller.dto.mapper.toDeck
import io.github.anki.anki.controller.dto.mapper.toDto
import io.github.anki.anki.service.DeckService
import io.github.anki.anki.service.model.Deck
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable

@RestController
@RequestMapping("api/v1/decks")
class DecksController (
    private val service: DeckService
){
    private val requestUserId = "66a11305dc669eefd22b5f3a"

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createDeck(@Valid @RequestBody request: NewDeckRequest): DeckDtoResponse {
        val newDeck: Deck = service.createNewDeck(request.toDeck(requestUserId))
        return newDeck.toDto()
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getDecks(): List<DeckDtoResponse> = service.getDecks(requestUserId).map { it.toDto() }

    @PatchMapping("/{deckId}")
    @ResponseStatus(HttpStatus.OK)
    fun patchDeck(@Valid @RequestBody request: PatchDeckRequest, @PathVariable deckId: String): DeckDtoResponse =
        service.updateDeck(request.toDeck(deckId = deckId, userId = requestUserId)).toDto()

    @DeleteMapping("/{deckId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCard(@PathVariable deckId: String) {
        service.deleteCard(deckId)
    }
}
