package io.github.anki.anki.controller

import io.github.anki.anki.controller.dto.DeckDtoResponse
import io.github.anki.anki.controller.dto.NewDeckRequest
import io.github.anki.anki.controller.dto.PatchDeckRequest
import io.github.anki.anki.controller.dto.mapper.toDeck
import io.github.anki.anki.controller.dto.mapper.toDto
import io.github.anki.anki.service.DeckService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future

@RestController
@RequestMapping("api/v1/decks")
class DecksController(
    private val service: DeckService,
    private var threadPool: ThreadPoolTaskExecutor,
) {
    private val requestUserId = "66a11305dc669eefd22b5f3a"

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createDeck(@Valid @RequestBody request: NewDeckRequest): DeckDtoResponse =
        runInThreadPoolAndThrowCause { service.createNewDeck(request.toDeck(requestUserId)).toDto() }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getDecks(): List<DeckDtoResponse> =
        runInThreadPoolAndThrowCause { service.getDecks(requestUserId).map { it.toDto() } }

    @PatchMapping("/{deckId}")
    @ResponseStatus(HttpStatus.OK)
    fun patchDeck(@Valid @RequestBody request: PatchDeckRequest, @PathVariable deckId: String): DeckDtoResponse =
        runInThreadPoolAndThrowCause {
            service.updateDeck(request.toDeck(deckId = deckId, userId = requestUserId)).toDto()
        }

    @DeleteMapping("/{deckId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteDeck(@PathVariable deckId: String) {
        runInThreadPoolAndThrowCause { service.deleteDeck(deckId, requestUserId) }
    }

    private fun <T> runInThreadPoolAndThrowCause(task: () -> T): T {
        try {
            val future: Future<T> = threadPool.submit(task)
            return future.get()
        } catch (e: ExecutionException) {
            throw e.cause!!
        }
    }
}
