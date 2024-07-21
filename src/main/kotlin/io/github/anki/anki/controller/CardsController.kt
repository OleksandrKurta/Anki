package io.github.anki.anki.controller

import io.github.anki.anki.controller.model.CardDto
import io.github.anki.anki.service.CardsService
import io.github.anki.anki.service.model.Card
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
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


    @GetMapping("/collection/{collectionId}")
    fun getCards(@PathVariable collectionId: String): ResponseEntity<Any> {
        val cards: List<Card> = service.getCards(collectionId)
        if (cards.isEmpty())
            return ResponseEntity<Any>(HttpStatus.NO_CONTENT)
        cards.forEach { it.toDto() }
        return ResponseEntity(cards, HttpStatus.OK)
    }

    @PostMapping
    fun createCard(@RequestBody card: CardDto): ResponseEntity<Any> {
        return ResponseEntity(service.addCard(card.toEntity()).toDto(), HttpStatus.OK)
    }

    @PatchMapping
    fun updateCard(@RequestBody card: CardDto): ResponseEntity<Any> {
        val updatedDto = service.updateCard(card.toEntity()).toDto()
        return ResponseEntity(updatedDto, HttpStatus.CREATED)

    }

    @DeleteMapping("/{id}")
    fun deleteCard(@PathVariable id: String): ResponseEntity<Any> {
        return ResponseEntity(service.deleteCard(id), HttpStatus.OK)
    }


}
