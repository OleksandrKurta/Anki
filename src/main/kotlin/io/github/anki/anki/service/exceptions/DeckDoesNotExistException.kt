package io.github.anki.anki.service.exceptions

class DeckDoesNotExistException(
    override val message: String = "Deck does not exist",
    override val cause: Throwable? = null,
) : BaseBadRequestException, RuntimeException(message, cause) {

    companion object {
        fun fromDeckIdAndUserId(deckId: String, userId: String) =
            DeckDoesNotExistException("Deck was not found with given deckId = $deckId and userId = $userId")

        fun fromDeckId(deckId: String) =
            DeckDoesNotExistException("Deck was not found with given deckId = $deckId")
    }
}
