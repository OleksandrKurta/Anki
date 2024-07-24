package io.github.anki.anki.controller

import io.github.anki.anki.controller.model.CardDto
import io.github.anki.anki.controller.model.NewCardDto
import io.github.anki.anki.controller.model.mapDtoToService
import io.github.anki.anki.controller.model.mapNewDtoToCard
import io.github.anki.anki.service.CardsService
import io.github.anki.anki.service.model.mapServiceToDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestBody


@RestController
@RequestMapping(("/api/v1/cards"))
class CardsController(
    private val service: CardsService) {

    @PostMapping
    fun createCard(@RequestBody card: NewCardDto): ResponseEntity<Any> {
        return ResponseEntity(mapServiceToDto(service.addCard(mapNewDtoToCard(card))), HttpStatus.CREATED)
    }

    @PatchMapping
    fun updateCard(@RequestBody card: CardDto): ResponseEntity<Any> {
        val updatedCard = service.updateCard(mapDtoToService(card)) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        return ResponseEntity(mapServiceToDto(updatedCard), HttpStatus.OK)
    }

    @DeleteMapping("/{id}")
    fun deleteCard(@PathVariable id: String): ResponseEntity<Any> {
        val deletedId = service.deleteCard(id) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        return ResponseEntity(deletedId, HttpStatus.OK)
    }
}
