package io.github.anki.anki.service

import io.github.anki.anki.api.nats.v1.deck.event.DeckEvent
import io.github.anki.anki.repository.mongodb.DeckEventRepository
import io.github.anki.anki.repository.mongodb.document.MongoDeckEvent
import io.github.anki.anki.service.utils.toObjectId
import io.nats.client.Message
import io.nats.client.MessageHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class DeckEventService(
    private val deckEventRepository: DeckEventRepository,
) : MessageHandler {

    override fun onMessage(msg: Message) {
        val deckEvent: DeckEvent = DeckEvent.parseFrom(msg.data)
        LOG.info("Received deck event: $deckEvent")
        val mongoDeckEvent: MongoDeckEvent =
            MongoDeckEvent(
                deckId = deckEvent.deckId.toObjectId(),
                event = deckEvent.deckEventType.name,
            )
        deckEventRepository.insert(mongoDeckEvent).subscribe()
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(DeckEventService::class.java)
    }
}
