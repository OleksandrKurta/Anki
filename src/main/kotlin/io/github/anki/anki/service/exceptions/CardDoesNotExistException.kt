package io.github.anki.anki.service.exceptions

class CardDoesNotExistException(
    override val message: String = "Card does not exist",
    override val cause: Throwable? = null,
) : BaseBadRequestException, RuntimeException(message, cause) {

    companion object {
        fun fromCardId(cardId: String) = CardDoesNotExistException("Card was not found with given id = $cardId")
    }
}
