package io.github.anki.anki.controller.dto.mapper

import io.github.anki.anki.controller.dto.EntityDtoResponse
import io.github.anki.anki.controller.dto.NewCardRequest
import io.github.anki.anki.controller.dto.PatchCardRequest
import service.model.CardLearningEntity


fun NewCardRequest.toCardEntity(deckId: String): CardLearningEntity =
    CardLearningEntity.buildFromDto(
        deckId,
        this.key,
        this.value,
    )


fun PatchCardRequest.toCardEntity(cardId: String, deckId: String): CardLearningEntity =
    CardLearningEntity.buildFromPatchDto(
        cardId,
        deckId,
        this.key,
        this.value,
    )

fun CardLearningEntity.toEntityDto(): EntityDtoResponse {
    var hint = this.hint ?: throw IllegalArgumentException("Card hint can not be null")
    var answer = this.answer ?: throw IllegalArgumentException("Card answer can not be null")
    return EntityDtoResponse(
        id = this.id ?: throw IllegalArgumentException("Card id can not be null"),
        deckId = this.deckId,
        key = hint.item.toString(),
        value = answer.item.toString(),
        lastRateId = this.rateId,
    )
}
