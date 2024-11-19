package io.github.anki.anki.controller.dto.mapper

import io.github.anki.anki.controller.dto.EntityDtoResponse
import io.github.anki.anki.controller.dto.NewCardRequest
import io.github.anki.anki.controller.dto.PatchCardRequest
import io.github.anki.testing.getRandomID
import io.github.anki.testing.getRandomString
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
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
import service.model.CardLearningEntity
import service.model.LearnItem
import java.time.Instant
import java.util.stream.Stream
import kotlin.test.BeforeTest

@Tag("unit")
class CardDtoMapperTest {
    private lateinit var randomCardID: ObjectId
    private lateinit var randomDeckID: ObjectId
    private lateinit var randomRateID: ObjectId
    private lateinit var randomCardKey: String
    private lateinit var randomCardValue: String
    private lateinit var lastRateID: ObjectId

    private val validator: Validator = Validation.buildDefaultValidatorFactory().validator

    @BeforeTest
    fun setUp() {
        randomCardID = getRandomID()
        randomDeckID = getRandomID()
        randomRateID = getRandomID()
        lastRateID = getRandomID()
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
                CardLearningEntity(
                    null,
                    null,
                    randomDeckID.toString(),
                    LearnItem(randomCardKey),
                    LearnItem(randomCardValue),
                    null,
                )

            // WHEN
            val actual: CardLearningEntity = newCardRequest.toCardEntity(randomDeckID.toString())

            // THEN
            actual.hint.item shouldBe expectedCard.hint.item
            actual.answer.item shouldBe expectedCard.answer.item

            actual.id shouldBe null
        }

        @ParameterizedTest
        @MethodSource("invalidNewCardRequestProvider")
        fun `should be error if cardKey is not valid`(cardKeyValue: Any) {
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
        fun `should be error if cardValue is not valid`(cardValueValue: Any?) {
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
                CardLearningEntity(
                    randomCardID.toString(),
                    null,
                    randomDeckID.toString(),
                    LearnItem(randomCardKey),
                    LearnItem(randomCardValue),
                    null,
                )

            // WHEN
            val actualCard: CardLearningEntity =
                patchCardRequest.toCardEntity(
                    cardId = randomCardID.toString(),
                    deckId = randomDeckID.toString(),

                )

            // THEN
            actualCard shouldBeEqualToComparingFields expectedCard
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
               CardLearningEntity(
                    randomCardID.toString(),
                    randomRateID.toString(),
                    randomDeckID.toString(),
                    LearnItem(randomCardKey),
                    LearnItem(randomCardValue),
                    null,
                )
            val expectedCard =
                EntityDtoResponse(
                    id = randomCardID.toString(),
                    deckId = randomDeckID.toString(),
                    key = randomCardKey,
                    value = randomCardValue,
                    lastRateId = randomRateID.toString()
                )

            // when
            val actual = card.toEntityDto()

            // then
            actual shouldBe expectedCard

            actual.id shouldNotBe null
        }

        @Test
        fun `should be error if id is null`() {
            // given
            val card =
                CardLearningEntity(
                    null,
                    randomRateID.toString(),
                    randomDeckID.toString(),
                    LearnItem(randomCardKey),
                    LearnItem(randomCardValue),
                    null,
                )

            // when/then
            shouldThrowExactly<IllegalArgumentException> {
                card.toEntityDto()
            }
        }

        @Test
        fun `should be error if key is null`() {
            // given
            val card =
                CardLearningEntity(
                    randomCardID.toString(),
                    randomRateID.toString(),
                    randomDeckID.toString(),
                    null,
                    LearnItem(randomCardValue),
                    null,
                )

            // when/then
            shouldThrowExactly<IllegalArgumentException> {
                card.toEntityDto()
            }
        }

        @Test
        fun `should be error if value is null`() {
            // given
            val card =
                CardLearningEntity(
                    randomCardID.toString(),
                    randomRateID.toString(),
                    randomDeckID.toString(),
                    LearnItem(randomCardKey),
                    null,
                    null,
                )

            // when/then
            shouldThrowExactly<IllegalArgumentException> {
                card.toEntityDto()
            }
        }
    }
}
