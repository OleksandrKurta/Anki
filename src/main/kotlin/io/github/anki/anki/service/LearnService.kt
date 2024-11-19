package io.github.anki.anki.service

import io.github.anki.anki.service.model.mapper.toCardEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import service.exceptions.RateLogExecutionException
import service.model.CardLearningEntity
import service.model.RateEnum
import service.model.RateLog
import java.util.*

@Service
@Scope(scopeName = "learnScope")
class LearnService @Autowired constructor(
    private val cardsService: CardsService,
    private val rateLogService: RateLogService,
    private val manager: LearnPriorityQueueManager
) {

    fun nextEntity(userId: String, deckId: String): CardLearningEntity? {
        var queue = manager.getSourceQueue(deckId)
        if (queue != null) {
            return queue.next()
        }
        return queue
    }

    @Throws(RateLogExecutionException::class)
    fun rateLearnEntity(rate: RateLog): RateLog {
        val rateLog = rateLogService.createRateLog(rate)
        val entity = cardsService.updateCardEntity(rateLog.userId, rateLog.toCardEntity())
        if (rateLog.learningRate.rate == RateEnum.AGAIN) {
            var queue = manager.getSourceQueue(rateLog.deckId)
            if (queue != null) {
                queue.add(entity)
            }
        }
        return rateLog
    }
}