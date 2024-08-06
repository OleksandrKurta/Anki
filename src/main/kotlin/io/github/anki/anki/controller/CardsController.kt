package io.github.anki.anki.controller

import io.github.anki.anki.controller.dto.CardDtoResponse
import io.github.anki.anki.controller.dto.NewCardRequest
import io.github.anki.anki.controller.dto.mapper.toCard
import io.github.anki.anki.controller.dto.mapper.toDto
import io.github.anki.anki.service.CardsService
import io.github.anki.anki.service.model.Card
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/cards")
class CardsController(
    private val service: CardsService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createCard(@RequestBody request: NewCardRequest): CardDtoResponse {
        val newCard: Card = service.createNewCard(request.toCard())
        return newCard.toDto()
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCard(@PathVariable id: String) {
        service.deleteCard(id)
    }
}
