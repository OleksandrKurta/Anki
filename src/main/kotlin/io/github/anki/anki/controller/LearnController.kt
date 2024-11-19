package io.github.anki.anki.controller

import io.github.anki.anki.controller.dto.EntityDtoResponse
import io.github.anki.anki.controller.dto.RateLogDto
import io.github.anki.anki.controller.dto.RateLogDtoResponse
import io.github.anki.anki.controller.dto.mapper.toDto
import io.github.anki.anki.controller.dto.mapper.toEntityDto
import io.github.anki.anki.controller.dto.mapper.toRateLog
import io.github.anki.anki.service.LearnService
import io.github.anki.anki.service.secure.SecurityService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import service.exceptions.RateLogExecutionException
import service.model.LearningEntity


@RestController
@Primary
@RequestMapping(LearnController.BASE_URL)
class LearnController @Autowired constructor(
    private val learnService: LearnService,
    private val securityService: SecurityService
) {

    @GetMapping(NEXT)
    @ResponseStatus(HttpStatus.OK)
    fun getNextLearnByDeck(@PathVariable deckId: String, @RequestHeader header: HttpHeaders?): EntityDtoResponse? {
        LOG.info(String.format("IN: LearnController %s get next object to learn", BASE_URL))
        val userId = securityService.jwtUtils.getUserIdFromAuthHeader(header!!)
        val card = learnService.nextEntity(userId, deckId)
        LOG.info(String.format("OUT: LearnController %s get next object to learn %s", BASE_URL, card))
        if (card != null) {
            return card.toEntityDto()
        }
        return null
    }

    @PostMapping(RATE_CONCRETE_OBJECT)
    @ResponseStatus(HttpStatus.OK)
    @Throws(
        RateLogExecutionException::class
    )
    fun rateLearnObject(
        @PathVariable deckId: String,
        @PathVariable objectId: String,
        @RequestHeader header: HttpHeaders?,
        @RequestBody rateDto: RateLogDto
    ): RateLogDtoResponse {
        LOG.info(String.format("IN: LearnController %s post rate", BASE_URL))
        val userId = securityService.jwtUtils.getUserIdFromAuthHeader(header!!)
        val rate = learnService.rateLearnEntity(rateDto.toRateLog(userId, deckId, objectId))
        LOG.info(String.format("OUT: LearnController %s post rate with id %s ", BASE_URL, rate.id))
        return rate.toDto()
    }

    companion object {
        val LOG: Logger = LoggerFactory.getLogger(LearnController::class.java)
        const val BASE_URL: String = "/api/v1/decks/{deckId}/learn"
        const val NEXT: String = "/next"
        const val RATE_CONCRETE_OBJECT: String = "/rate/{objectId}"
    }
}
