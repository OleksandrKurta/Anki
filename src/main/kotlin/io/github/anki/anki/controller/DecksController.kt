package io.github.anki.anki.controller

import io.github.anki.anki.controller.dto.DeckDtoResponse
import io.github.anki.anki.controller.dto.NewDeckRequest
import io.github.anki.anki.controller.dto.mapper.toDeck
import io.github.anki.anki.controller.dto.mapper.toDto
import io.github.anki.anki.service.DeckService
import io.github.anki.anki.service.model.Deck
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController("api/v1/decks")
class DecksController (
    private val service: DeckService
){
//    @PostMapping
//    @ResponseStatus(HttpStatus.CREATED)
//    fun createDeck(@Valid @RequestBody request: NewDeckRequest): DeckDtoResponse {
//        val requestUserId = "66a11305dc669eefd22b5f3a"
//        val newDeck: Deck = service.createNewCard(request.apply { userId = requestUserId }.toDeck())
//        return newDeck.toDto()
//    }

//    @DeleteMapping("/{id}")
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    fun deleteCard(@PathVariable id: String) {
//        service.deleteCard(id)
//    }
}
