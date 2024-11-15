package io.github.anki.anki.controller

import io.github.anki.anki.api.nats.v1.deck.event.NatsSubject
import io.github.anki.anki.service.DeckEventService
import io.nats.client.Connection
import io.nats.client.Dispatcher
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Controller

@Controller
class DeckEventNatsController(
    private val natsClient: Connection,
    private val deckEventService: DeckEventService,
) : NatsController {

    @PostConstruct
    override fun subscribeToSubject() {
        val dispatcher: Dispatcher = natsClient.createDispatcher(deckEventService)
        dispatcher.subscribe(NatsSubject.DECK_EVENT_SUBJECT)
    }
}
