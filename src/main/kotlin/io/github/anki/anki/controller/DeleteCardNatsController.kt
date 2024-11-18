package io.github.anki.anki.controller

import io.github.anki.anki.api.nats.nats.NatsSubject
import io.github.anki.anki.service.DeleteCardService
import io.nats.client.Connection
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Controller

@Controller
class DeleteCardNatsController(
    private val natsClient: Connection,
    private val deleteCardService: DeleteCardService,
) {

    @PostConstruct
    fun deleteCard() {
        natsClient.createDispatcher(deleteCardService).subscribe(NatsSubject.Card.CARD_DELETE_COMMAND)
    }
}
