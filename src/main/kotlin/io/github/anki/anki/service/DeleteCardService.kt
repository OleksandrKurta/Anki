package io.github.anki.anki.service

import io.github.anki.anki.api.nats.v1.card.commands.DeleteCardRequest
import io.github.anki.anki.api.nats.v1.card.commands.DeleteCardResponse
import io.github.anki.anki.api.nats.v1.card.commands.Failure
import io.github.anki.anki.api.nats.v1.card.commands.Success
import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.service.utils.toObjectId
import io.nats.client.Connection
import io.nats.client.Message
import io.nats.client.MessageHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.kotlin.core.publisher.toMono

@Service
class DeleteCardService(
    private val cardRepository: CardRepository,
    private val natsClient: Connection,
) : MessageHandler {

    override fun onMessage(msg: Message) {
        msg.toMono()
            .map { DeleteCardRequest.parseFrom(msg.data) }
            .doOnNext { LOG.info("Received DeleteCardRequest: $it") }
            .flatMap { cardRepository.softDelete(it.cardId.toObjectId()) }
            .doOnSuccess { natsClient.publish(msg.replyTo, successResponse().toByteArray()) }
            .doOnError { natsClient.publish(msg.replyTo, failureResponse(it).toByteArray()) }
            .subscribe()
    }

    private fun failureResponse(err: Throwable): DeleteCardResponse =
        DeleteCardResponse.newBuilder()
            .setFailure(Failure.newBuilder().setReason(err.message))
            .build()

    private fun successResponse(): DeleteCardResponse =
        DeleteCardResponse.newBuilder()
            .setSuccess(Success.newBuilder().build())
            .build()

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(DeleteCardService::class.java)
    }
}
