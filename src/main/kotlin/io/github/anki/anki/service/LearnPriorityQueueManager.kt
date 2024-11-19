package io.github.anki.anki.service

import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.DeckRepository
import io.github.anki.anki.repository.mongodb.PresetRepository
import io.github.anki.anki.service.comparators.PresetComparator
import io.github.anki.anki.service.model.Preset
import io.github.anki.anki.service.model.mapper.toCardEntity
import io.github.anki.anki.service.model.mapper.toPreset
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import service.model.CardLearningEntity
import service.model.LearningEntity
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.Comparator

interface BaseLearnQueue {
    fun fillQueue(comparator: Comparator<CardLearningEntity>, sourceId: String)
}

@Component
@Scope("prototype")
class LearnPriorityIterator (
    private val cardRepository: CardRepository,
    private val deckRepository: DeckRepository,
    private val presetRepository: PresetRepository,
) : MutableIterator<LearningEntity>,  BaseLearnQueue{
    private lateinit var learnQueue: PriorityQueue<CardLearningEntity>
    private var currentPreset: Preset? = null


    override fun fillQueue(comparator: Comparator<CardLearningEntity>, sourceId: String) {
        var presetId: ObjectId?  = deckRepository.findById(ObjectId(sourceId)).get()?.presetId
        var currentMongoPreset = presetId?.let { presetRepository.findPresetById(it) }?.get()
        this.currentPreset = currentMongoPreset?.toPreset()
        learnQueue = PriorityQueue(comparator)

        var cards = currentPreset?.let {
            cardRepository
                .findByDeckIdAscDateWithLimit(ObjectId(sourceId), limit= it.maxCardsPerDay)
                .get()
                .map { it.toCardEntity() }
        }
        if (cards != null) {
            cards.forEach { learnQueue.add(it) }
        }
    }

    override fun hasNext(): Boolean {
        return learnQueue.isEmpty()
    }

    override fun next(): CardLearningEntity {
        return learnQueue.poll()
    }

    override fun remove() {
        learnQueue.remove()
    }

    fun add(element: CardLearningEntity?) {
        learnQueue.add(element)
    }


}


@Component
@Scope(scopeName = "learnScope")
class LearnPriorityQueueManager @Autowired constructor(
    private val learnQueue: LearnPriorityIterator,
    private val presetComparator: PresetComparator,
) {
    private var learnQueueMap: ConcurrentHashMap<String, LearnPriorityIterator> = ConcurrentHashMap()

    fun getSourceQueue(sourceId: String): LearnPriorityIterator? {
        if (learnQueueMap.containsKey(sourceId)) {
            return learnQueueMap.get(sourceId)
        }
        else {
            var newQueue = learnQueue
            newQueue.fillQueue(presetComparator, sourceId)
            learnQueueMap.put(sourceId, learnQueue)
            return newQueue
        }
    }


}