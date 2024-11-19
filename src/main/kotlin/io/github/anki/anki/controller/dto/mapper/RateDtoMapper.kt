package io.github.anki.anki.controller.dto.mapper

import io.github.anki.anki.controller.dto.RateLogDto
import io.github.anki.anki.controller.dto.RateLogDtoResponse
import service.model.CardRate
import service.model.RateLog

fun RateLogDto.toRateLog(
    userId: String,
    deckId: String,
    objectId: String,
): RateLog =
    RateLog(CardRate(this.rate), deckId, objectId, userId)

fun RateLog.toDto(): RateLogDtoResponse = RateLogDtoResponse(this.id)
