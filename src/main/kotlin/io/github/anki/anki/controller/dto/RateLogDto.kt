package io.github.anki.anki.controller.dto

import service.model.RateEnum

data class RateLogDto(
    var rate: RateEnum,
)

data class RateLogDtoResponse(val rateLogId: String)
