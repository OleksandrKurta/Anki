package io.github.anki.anki.service.comparators

import org.springframework.stereotype.Component
import service.model.CardLearningEntity
import java.time.Duration
import java.time.Instant


@Component
class PresetComparator : Comparator<CardLearningEntity> {
    override fun compare(o1: CardLearningEntity, o2: CardLearningEntity): Int {
        val now = Instant.now()
        val time1 = o1.lastLearn
        val time2 = o2.lastLearn

        if (time1 == null && time2 == null) return 0
        if (time1 == null) return -1
        if (time2 == null) return 1

        val delta1 = Duration.between(time1, now).toMillis()
        val delta2 = Duration.between(time2, now).toMillis()

        return java.lang.Long.compare(delta2, delta1)
    }
}