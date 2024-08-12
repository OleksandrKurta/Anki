package io.github.anki.anki.controller.dto.mapper

import io.github.anki.anki.controller.dto.CardDtoResponse
import io.github.anki.anki.controller.dto.NewCardRequest
import io.github.anki.anki.controller.dto.PatchCardRequest
import io.github.anki.anki.service.model.Card
import io.github.anki.testing.getRandomID
import io.github.anki.testing.getRandomString
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.bson.types.ObjectId
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.BeforeTest

@Tag("unit")
class CardDtoMapperTest {
    private lateinit var randomCardID: ObjectId
    private lateinit var randomDeckID: ObjectId
    private lateinit var randomCardKey: String
    private lateinit var randomCardValue: String

    private val validator: Validator = Validation.buildDefaultValidatorFactory().validator

    @BeforeTest
    fun setUp() {
        randomCardID = getRandomID()
        randomDeckID = getRandomID()
        randomCardKey = getRandomString()
        randomCardValue = getRandomString()
    }

    @Nested
    @DisplayName("NewCardRequest.toCard()")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class NewCardRequestToCard {
        @Test
        fun `should map NewCardRequest to Card`() {
            // GIVEN
            val newCardRequest =
                NewCardRequest(
                    key = randomCardKey,
                    value = randomCardValue,
                )
            val expectedCard =
                Card(
                    deckId = randomDeckID.toString(),
                    key = randomCardKey,
                    value = randomCardValue,
                )

            // WHEN
            val actual: Card = newCardRequest.toCard(randomDeckID.toString())

            // THEN
            actual.shouldBeEqualToIgnoringFields(expectedCard, Card::id)

            actual.id shouldBe null
        }

        @ParameterizedTest
        @MethodSource("invalidNewCardRequestProvider")
        fun `should be error if cardKey is not valid`(cardKeyValue: String?) {
            // given
            val newCardRequest =
                NewCardRequest(
                    key = cardKeyValue,
                    value = randomCardValue,
                )

            // when
            val violations = validator.validate(newCardRequest)

            // then
            violations.size shouldBe 1

            violations.first().propertyPath.toString() shouldBe "key"

            violations.first().message shouldBe "must not be blank"
        }

        @ParameterizedTest
        @MethodSource("invalidNewCardRequestProvider")
        fun `should be error if cardValue is not valid`(cardValueValue: String?) {
            // given
            val newCardRequest =
                NewCardRequest(
                    key = randomCardKey,
                    value = cardValueValue,
                )

            // when
            val violations = validator.validate(newCardRequest)

            // then
            violations.size shouldBe 1

            violations.first().propertyPath.toString() shouldBe "value"

            violations.first().message shouldBe "must not be blank"
        }

        @Suppress("UnusedPrivateMember")
        private fun invalidNewCardRequestProvider(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(null),
                Arguments.of(""),
            )
        }
    }

    @Nested
    @DisplayName("PatchCardRequest.toCard()")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class PatchCardRequestToCard {
        @Test
        fun `should map PatchCardRequest to Card`() {
            // GIVEN
            val patchCardRequest =
                PatchCardRequest(
                    key = randomCardKey,
                    value = randomCardValue,
                )
            val expectedCard =
                Card(
                    id = randomCardID.toString(),
                    deckId = randomDeckID.toString(),
                    key = randomCardKey,
                    value = randomCardValue,
                )

            // WHEN
            val actualCard: Card =
                patchCardRequest.toCard(
                    cardId = randomCardID.toString(),
                    deckId = randomDeckID.toString(),
                )

            // THEN
            actualCard shouldBe expectedCard
        }
    }

    @Nested
    @DisplayName("Card.toDto()")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class CardToDto {
        @Test
        fun `should map Card to CardDtoResponse`() {
            // given
            val card =
                Card(
                    id = randomCardID.toString(),
                    deckId = randomDeckID.toString(),
                    key = randomCardKey,
                    value = randomCardValue,
                )
            val expectedCard =
                CardDtoResponse(
                    id = randomCardID.toString(),
                    deckId = randomDeckID.toString(),
                    key = randomCardKey,
                    value = randomCardValue,
                )

            // when
            val actual = card.toDto()

            // then
            actual shouldBe expectedCard

            actual.id shouldNotBe null
        }
    }
}
