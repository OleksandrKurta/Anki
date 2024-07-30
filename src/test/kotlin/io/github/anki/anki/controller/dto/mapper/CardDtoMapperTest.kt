package io.github.anki.anki.controller.dto.mapper

import io.github.anki.anki.controller.dto.NewCardRequest
import io.github.anki.anki.service.model.mapper.toCard

import io.github.anki.anki.repository.mongodb.document.MongoCard
import io.github.anki.anki.service.model.Card
import io.github.anki.testing.getRandomID
import io.github.anki.testing.getRandomString
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.bson.types.ObjectId
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import kotlin.test.BeforeTest

class CardDtoMapperTest {

    private lateinit var randomCardID: ObjectId
    private lateinit var randomDeckID: ObjectId
    private lateinit var randomCardKey: String
    private lateinit var randomCardValue: String

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
            val newCardRequest = NewCardRequest(
                deckId = randomDeckID.toString(),
                cardKey = randomCardKey,
                cardValue = randomCardValue,
            )
            val expectedCard = Card(
                deckId = randomDeckID.toString(),
                cardKey = randomCardKey,
                cardValue = randomCardValue,
            )

            // WHEN
            val actual: Card = newCardRequest.toCard()

            // THEN
            actual.shouldBeEqualToIgnoringFields(expectedCard, Card::id)

            actual.id shouldBe null
        }

    }

    @Nested
    @DisplayName("Card.toDto()")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class CardToDto {
        @Test
        fun `should map mongo to card if id is null`() {
            //given
            val mongoCard = MongoCard(
                deckId = randomDeckID,
                cardKey = randomCardKey,
                cardValue = randomCardValue,
            )
            val expectedCard = Card(
                deckId = randomDeckID.toString(),
                cardKey = randomCardKey,
                cardValue = randomCardValue,
            )

            //when
            val actual = mongoCard.toCard()

            //then
            actual shouldBe expectedCard

            actual.id shouldBe null

        }

        @Test
        fun `should map mongo to card if id is NOT null`() {
            //given
            val mongoCard = MongoCard(
                id = randomCardID,
                deckId = randomDeckID,
                cardKey = randomCardKey,
                cardValue = randomCardValue,
            )
            val expectedCard = Card(
                id = randomCardID.toString(),
                deckId = randomDeckID.toString(),
                cardKey = randomCardKey,
                cardValue = randomCardValue,
            )

            //when
            val actual = mongoCard.toCard()

            //then
            actual shouldBe expectedCard

            actual.id shouldNotBe null

        }
    }
}
