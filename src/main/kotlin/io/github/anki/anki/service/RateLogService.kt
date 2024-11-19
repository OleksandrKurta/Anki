package io.github.anki.anki.service

import io.github.anki.anki.repository.mongodb.RateLogRepository
import io.github.anki.anki.service.model.mapper.toMongo
import io.github.anki.anki.service.model.mapper.toRate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import service.exceptions.RateLogExecutionException
import service.model.RateLog



@Service
class RateLogService @Autowired constructor(
    private val rateLogRepository: RateLogRepository) {
    @Throws(RateLogExecutionException::class)
    fun createRateLog(rateLog: RateLog): RateLog {
        return rateLogRepository.insert(rateLog.toMongo()).get().toRate()
    }
}