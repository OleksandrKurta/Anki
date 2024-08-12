package io.github.anki.anki.controller.dto.mapper

import io.github.anki.anki.controller.dto.DeckDtoResponse
import io.github.anki.anki.controller.dto.NewDeckRequest
import io.github.anki.anki.controller.dto.PatchDeckRequest
import io.github.anki.anki.service.model.Deck
import io.github.anki.testing.getRandomID
import io.github.anki.testing.getRandomString
import io.kotest.matchers.shouldBe
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.bson.types.ObjectId
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.BeforeTest
import kotlin.test.Test

class DeckDtoMapperTest {
    private lateinit var randomUserID: ObjectId
    private lateinit var randomDeckID: ObjectId
    private lateinit var randomDeckName: String
    private lateinit var randomDeckDescription: String

    private val mockUserId = "66a11305dc669eefd22b5f3a"

    private val validator: Validator = Validation.buildDefaultValidatorFactory().validator

    @BeforeTest
    fun setUp() {
        randomUserID = getRandomID()
        randomDeckID = getRandomID()
        randomDeckName = getRandomString()
        randomDeckDescription = getRandomString()
    }

    @Nested
    @DisplayName("NewDeckRequest.toDeck()")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class NewDeckRequestToDeck {
        @Test
        fun `should map NewDeckRequest to Deck`() {
            // given
            val newDeckRequest =
                NewDeckRequest(
                    name = randomDeckName,
                    description = randomDeckDescription,
                )
            val expectedDeck =
                Deck(
                    userId = mockUserId,
                    name = randomDeckName,
                    description = randomDeckDescription,
                )

            // when
            val actualDeck = newDeckRequest.toDeck(mockUserId)

            // then
            actualDeck shouldBe expectedDeck

            actualDeck.id shouldBe null
        }

        @ParameterizedTest
        @MethodSource("invalidNewDeckRequestProvider")
        fun `should be error if name is not valid`(nameValue: String?) {
            // given
            val newDeckRequest =
                NewDeckRequest(
                    name = nameValue,
                    description = randomDeckDescription,
                )

            // when
            val violations = validator.validate(newDeckRequest)

            // then
            violations.size shouldBe 1

            violations.first().propertyPath.toString() shouldBe "name"

            violations.first().message shouldBe "must not be blank"
        }

        @Suppress("UnusedPrivateMember")
        private fun invalidNewDeckRequestProvider(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(null),
                Arguments.of(""),
            )
        }
    }

    @Nested
    @DisplayName("PatchDeckRequest.toDeck()")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class PatchDeckRequestToDeck {
        @org.junit.jupiter.api.Test
        fun `should map PatchDeckRequest to Deck`() {
            // GIVEN
            val patchDeckRequest =
                PatchDeckRequest(
                    name = randomDeckName,
                    description = randomDeckDescription,
                )
            val expectedDeck =
                Deck(
                    id = randomDeckID.toString(),
                    userId = randomUserID.toString(),
                    name = randomDeckName,
                    description = randomDeckDescription,
                )

            // WHEN
            val actualDeck: Deck =
                patchDeckRequest.toDeck(
                    deckId = randomDeckID.toString(),
                    userId = randomUserID.toString(),
                )

            // THEN
            actualDeck shouldBe expectedDeck
        }
    }

    @Nested
    @DisplayName("Deck.toDto()")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class DeckToDeckDtoResponse {
        @Test
        fun `should map Deck to DeckDtoResponse`() {
            // given
            val deck =
                Deck(
                    id = randomDeckID.toString(),
                    userId = randomUserID.toString(),
                    name = randomDeckName,
                    description = randomDeckDescription,
                )

            val expectedDeckDtoResponse =
                DeckDtoResponse(
                    id = randomDeckID.toString(),
                    userId = randomUserID.toString(),
                    name = randomDeckName,
                    description = randomDeckDescription,
                )

            // when
            val actualDeckDtoResponse = deck.toDto()

            // then

            actualDeckDtoResponse shouldBe expectedDeckDtoResponse
        }
    }
}
