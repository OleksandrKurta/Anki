package io.github.anki.anki.controller

import io.github.anki.anki.models.Card
import io.github.anki.anki.service.CardsService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus


@RestController
@RequestMapping(("/api/v1/cards"))
class CardsController(private val service: CardsService) {

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(e: NoSuchElementException): ResponseEntity<String> =
        ResponseEntity(e.message, HttpStatus.NOT_FOUND)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(e: IllegalArgumentException): ResponseEntity<String> =
        ResponseEntity(e.message, HttpStatus.BAD_REQUEST)


    @GetMapping("/{id}")
    fun getCard(@PathVariable id: Int): Card = service.getCard(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createCard(@RequestBody card: Card): Card = service.addCard(card)

    @PatchMapping
    fun updateCard(@RequestBody card: Card): Card = service.updateCard(card)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCard(@PathVariable id: Int): Unit = service.deleteCard(id)


}
