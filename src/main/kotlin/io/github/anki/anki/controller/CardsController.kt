package io.github.anki.anki.controller

import io.github.anki.anki.controller.dto.CardDtoResponse
import io.github.anki.anki.controller.dto.NewCardRequest
import io.github.anki.anki.controller.dto.PaginationDto
import io.github.anki.anki.controller.dto.PatchCardRequest
import io.github.anki.anki.controller.dto.mapper.toCard
import io.github.anki.anki.controller.dto.mapper.toDto
import io.github.anki.anki.controller.dto.mapper.toPagination
import io.github.anki.anki.service.CardsService
import io.github.anki.anki.service.model.Card
import io.github.anki.anki.service.secure.SecurityService
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping(CardsController.BASE_URL)
class CardsController(
    private val cardService: CardsService,
    val securityService: SecurityService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createCard(
        @Valid @RequestBody request: NewCardRequest,
        @PathVariable deckId: String,
        @RequestHeader header: HttpHeaders,
    ): Mono<CardDtoResponse> =
        cardService.createNewCard(
            userId = securityService.getUserIdFromAuthHeader(header),
            request.toCard(deckId),
        )
            .doFirst { LOG.info("IN: $CardsController $BASE_URL create card in deck with id = $deckId") }
            .doOnNext { LOG.info("OUT: $CardsController $BASE_URL created card ${it.id} with id = $deckId") }
            .map(Card::toDto)

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getAllCardsFromDeck(
        @PathVariable deckId: String,
        @RequestHeader header: HttpHeaders,
        @RequestParam(
            name = PaginationDto.LIMIT,
            required = false,
            defaultValue = PaginationDto.DEFAULT_LIMIT.toString(),
        ) limit: Int,
        @RequestParam(
            name = PaginationDto.OFFSET,
            required = false,
            defaultValue = PaginationDto.DEFAULT_OFFSET.toString(),
        ) offset: Int,
    ): Mono<List<CardDtoResponse>> =
        cardService.findCardsByDeckWithPagination(
            deckId = deckId,
            userId = securityService.getUserIdFromAuthHeader(header),
            pagination = PaginationDto(limit, offset).toPagination(),
        )
            .doFirst { LOG.info("IN: $CardsController $BASE_URL get all cards in deck with id = $deckId") }
            .map(Card::toDto)
            .collectList()
            .doOnNext { LOG.info("OUT: $CardsController $BASE_URL got cards $it from deck with id = $deckId") }

    @PatchMapping(CONCRETE_CARD)
    @ResponseStatus(HttpStatus.OK)
    fun patchCard(
        @PathVariable deckId: String,
        @PathVariable cardId: String,
        @RequestHeader header: HttpHeaders,
        @RequestBody request: PatchCardRequest,
    ): Mono<CardDtoResponse> =
        cardService.updateCard(
            userId = securityService.getUserIdFromAuthHeader(header),
            request.toCard(cardId, deckId),
        )
            .doFirst {
                LOG.info("IN: $CardsController $BASE_URL patch card with id $cardId from deck with id = $deckId")
            }
            .doOnNext {
                LOG.info("OUT: $CardsController ${BASE_URL} patched card with id $cardId from deck with id = $deckId")
            }
            .map(Card::toDto)

    @DeleteMapping(CONCRETE_CARD)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCard(
        @PathVariable deckId: String,
        @PathVariable cardId: String,
        @RequestHeader header: HttpHeaders,
    ): Mono<Void> =
        cardService.deleteCard(
            deckId = deckId,
            userId = securityService.jwtUtils.getUserIdFromAuthHeader(header),
            cardId = cardId,
        )
            .doFirst {
                LOG.info("IN: $CardsController $BASE_URL delete card with id $cardId from deck with id = $deckId")
            }
            .doOnSuccess {
                LOG.info("OUT: $CardsController $BASE_URL deleted card with id $cardId from deck with id = $deckId")
            }

    companion object {
        val LOG = LoggerFactory.getLogger(CardsController::class.java)
        const val BASE_URL = "/api/v1/decks/{deckId}/cards"
        const val CONCRETE_CARD = "/{cardId}"
    }
}
