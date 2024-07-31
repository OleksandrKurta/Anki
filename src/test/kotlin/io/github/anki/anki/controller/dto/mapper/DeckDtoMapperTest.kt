package io.github.anki.anki.controller.dto.mapper

import io.github.anki.anki.controller.dto.DeckDtoResponse
import io.github.anki.anki.controller.dto.NewDeckRequest
import io.github.anki.anki.service.model.Deck
import io.github.anki.testing.getRandomID
import io.github.anki.testing.getRandomString
import io.kotest.matchers.shouldBe
import org.bson.types.ObjectId
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import kotlin.test.BeforeTest
import kotlin.test.Test

class DeckDtoMapperTest {

    private lateinit var randomUserID: ObjectId
    private lateinit var randomDeckID: ObjectId
    private lateinit var randomDeckName: String
    private lateinit var randomDeckDescription: String

    private val commonUserId = "66a11305dc669eefd22b5f3a"

    @BeforeTest
    fun setUp() {
        randomUserID = getRandomID()
        randomDeckID = getRandomID()
        randomDeckName = getRandomString()
        randomDeckDescription = getRandomString()
    }

    @Nested
    @DisplayName("NewDeckRequest.toCollection()")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class NewDeckRequestToDeck {
        @Test
        fun `should map NewDeckRequest to Deck`() {
            //given
            val newDeckRequest = NewDeckRequest(
                name = randomDeckName,
                description = randomDeckDescription,
            )
            val expectedDeck = Deck(
                userId = commonUserId,
                name = randomDeckName,
                description = randomDeckDescription,
            )

            //when
            val actualDeck = newDeckRequest.toDeck(commonUserId)

            //then

            actualDeck shouldBe expectedDeck

            actualDeck.id shouldBe null

        }
    }

    @Nested
    @DisplayName("Deck.toDto()")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class DeckToDeckDtoResponse {

        @Test
        fun `should map Deck to DeckDtoResponse`() {
            //given
            val deck = Deck(
                id = randomDeckID.toString(),
                userId = randomUserID.toString(),
                name = randomDeckName,
                description = randomDeckDescription,
            )

            val expectedDeckDtoResponse = DeckDtoResponse(
                id = randomDeckID.toString(),
                userId = randomUserID.toString(),
                name = randomDeckName,
                description = randomDeckDescription,
            )

            //when
            val actualDeckDtoResponse = deck.toDto()

            //then

            actualDeckDtoResponse shouldBe expectedDeckDtoResponse
        }
    }
}
