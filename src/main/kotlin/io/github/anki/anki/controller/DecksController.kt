package io.github.anki.anki.controller

import io.github.anki.anki.controller.dto.DeckDtoResponse
import io.github.anki.anki.controller.dto.NewDeckRequest
import io.github.anki.anki.controller.dto.PatchDeckRequest
import io.github.anki.anki.controller.dto.mapper.toDeck
import io.github.anki.anki.controller.dto.mapper.toDto
import io.github.anki.anki.service.DeckService
import io.github.anki.anki.service.secure.jwt.JwtUtils
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(DecksController.BASE_URL)
class DecksController(
    private val service: DeckService,
    private val jwtUtils: JwtUtils,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createDeck(
        @Valid @RequestBody request: NewDeckRequest,
        @RequestHeader header: HttpHeaders,
    ): DeckDtoResponse {
        LOG.info("IN: $DecksController ${BASE_URL} with name ${request.name}")
        val deck = service.createNewDeck(request.toDeck(jwtUtils.getUserIdFromAuthHeader(header)))
        LOG.info("OUT: $DecksController ${BASE_URL} create deck with id = ${deck.id}")
        return deck.toDto()
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getDecks(@RequestHeader header: HttpHeaders): List<DeckDtoResponse> {
        LOG.info("IN: $DecksController ${BASE_URL} get decks")
        val decks = service.getDecks(jwtUtils.getUserIdFromAuthHeader(header))
        LOG.info("OUT: $DecksController ${BASE_URL} get all decks")
        return decks.map { it.toDto() }
    }

    @PatchMapping(CONCRETE_DECK)
    @ResponseStatus(HttpStatus.OK)
    fun patchDeck(
        @Valid @RequestBody request: PatchDeckRequest,
        @PathVariable deckId: String,
        @RequestHeader header: HttpHeaders,
    ): DeckDtoResponse {
        LOG.info("IN: $DecksController ${BASE_URL} patch $request deck with id = $deckId")
        val deck =
            service.updateDeck(
                request.toDeck(deckId = deckId, userId = jwtUtils.getUserIdFromAuthHeader(header)),
            )
        LOG.info("OUT: $DecksController ${BASE_URL} patched deck with id = $deckId")
        return deck.toDto()
    }

    @DeleteMapping(CONCRETE_DECK)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteDeck(@PathVariable deckId: String, @RequestHeader header: HttpHeaders) {
        LOG.info("IN: $DecksController ${BASE_URL} delete deck with id = $deckId")
        val deck = service.deleteDeck(deckId, jwtUtils.getUserIdFromAuthHeader(header))
        LOG.info("OUT: $DecksController ${BASE_URL} deleted deck with id = $deckId")
        return deck
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(DecksController::class.java)

        const val BASE_URL = "/api/v1/decks"
        const val CONCRETE_DECK = "/{deckId}"
    }
}
