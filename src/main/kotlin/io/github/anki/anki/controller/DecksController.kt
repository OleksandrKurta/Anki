package io.github.anki.anki.controller

import io.github.anki.anki.controller.dto.DeckDtoResponse
import io.github.anki.anki.controller.dto.NewDeckRequest
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
import org.springframework.web.bind.annotation.PathVariable

@RestController
@RequestMapping("api/v1/decks")
class DecksController (
    private val service: DeckService
){

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createDeck(@Valid @RequestBody request: NewDeckRequest): DeckDtoResponse {
        val requestUserId = "66a11305dc669eefd22b5f3a"
        val newDeck: Deck = service.createNewDeck(request.toDeck(requestUserId))
        return newDeck.toDto()
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCard(@PathVariable id: String) {
        service.deleteCard(id)
    }
}
