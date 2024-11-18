package io.github.anki.anki.service

import io.github.anki.anki.api.kafka.v1.deck.DeckEvent
import io.github.anki.anki.repository.mongodb.DeckEventRepository
import io.github.anki.anki.repository.mongodb.document.MongoDeckEvent
import io.github.anki.anki.service.model.mapper.toMongoDeckEvent
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Service
class DeckEventService(
    private val deckEventRepository: DeckEventRepository,
) {

    fun saveDeckEvent(deckEvent: DeckEvent): Mono<MongoDeckEvent> =
        deckEvent.toMono()
            .map { it.toMongoDeckEvent() }
            .flatMap { deckEventRepository.insert(it) }
}
