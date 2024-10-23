package io.github.anki.anki.controller

import io.github.anki.anki.controller.dto.DeckDtoResponse
import io.github.anki.anki.controller.dto.NewDeckRequest
import io.github.anki.anki.controller.dto.PatchDeckRequest
import io.github.anki.anki.controller.dto.mapper.toDeck
import io.github.anki.anki.controller.dto.mapper.toDto
import io.github.anki.anki.service.DeckService
import io.github.anki.anki.service.model.Deck
import io.github.anki.anki.service.secure.SecurityService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
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
import reactor.core.publisher.Mono

@RestController
@RequestMapping(DecksController.BASE_URL)
class DecksController(
    private val service: DeckService,
    val securityService: SecurityService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createDeck(
        @Valid @RequestBody request: NewDeckRequest,
    ): Mono<DeckDtoResponse> =
        securityService
            .getUserIdFromAuthentication()
            .doFirst { LOG.info("IN: $DecksController $BASE_URL create deck with name ${request.name}") }
            .flatMap {
                service.createNewDeck(request.toDeck(it))
            }
            .map(Deck::toDto)
            .doOnNext { LOG.info("OUT: $DecksController $BASE_URL created deck with id = ${it.id}") }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getDecks(): Mono<List<DeckDtoResponse>> =
        securityService
            .getUserIdFromAuthentication()
            .doFirst { LOG.info("IN: $DecksController $BASE_URL get decks") }
            .flatMapMany { service.getDecks(it) }
            .map(Deck::toDto)
            .collectList()
            .doOnNext { LOG.info("OUT: $DecksController $BASE_URL get all decks $it") }

    @PatchMapping(CONCRETE_DECK)
    @ResponseStatus(HttpStatus.OK)
    fun patchDeck(
        @Valid @RequestBody request: PatchDeckRequest,
        @PathVariable deckId: String,
    ): Mono<DeckDtoResponse> =
        securityService
            .getUserIdFromAuthentication()
            .doFirst { LOG.info("IN: $DecksController $BASE_URL patch $request deck with id = $deckId") }
            .flatMap {
                service
                    .updateDeck(request.toDeck(deckId = deckId, userId = it))
            }
            .map(Deck::toDto)
            .doOnNext { LOG.info("OUT: $DecksController $BASE_URL patched deck with id = $deckId") }

    @DeleteMapping(CONCRETE_DECK)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteDeck(@PathVariable deckId: String): Mono<Void> =
        securityService
            .getUserIdFromAuthentication()
            .doFirst { LOG.info("IN: $DecksController $BASE_URL delete deck with id = $deckId") }
            .flatMap {
                service
                    .deleteDeck(deckId, it)
            }
            .doOnSuccess { LOG.info("OUT: $DecksController $BASE_URL deleted deck with id = $deckId") }

    companion object {
        private val LOG = LoggerFactory.getLogger(DecksController::class.java)

        const val BASE_URL = "/api/v1/decks"
        const val CONCRETE_DECK = "/{deckId}"
    }
}
