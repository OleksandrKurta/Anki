package io.github.anki.anki.service.model.mapper

import io.github.anki.anki.repository.mongodb.document.MongoCard

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
import service.model.CardLearningEntity
import service.model.LearnItem
import kotlin.test.BeforeTest

class CardMapperTest {
    private lateinit var randomCardID: ObjectId
    private lateinit var randomDeckID: ObjectId
    private lateinit var randomRateID: ObjectId
    private lateinit var randomCardKey: String
    private lateinit var randomCardValue: String

    @BeforeTest
    fun setUp() {
        randomCardID = getRandomID()
        randomDeckID = getRandomID()
        randomRateID = getRandomID()
        randomCardKey = getRandomString()
        randomCardValue = getRandomString()
    }

    @Nested
    @DisplayName("Card.toMongo()")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class CardToMongo {
        @Test
        fun `should map card to mongo when id is null`() {
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
            val expectedMongoCard =
                MongoCard(
                    deckId = randomDeckID,
                    key = randomCardKey,
                    value = randomCardValue,
                )

            // when
            val actual: MongoCard = card.toMongo()

            // then
            actual.shouldBeEqualToIgnoringFields(expectedMongoCard, MongoCard::id)

            actual.id shouldBe null
        }

        @Test
        fun `should map card to mongo when id is not null`() {
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
            val expectedMongoCard =
                MongoCard(
                    id = randomCardID,
                    deckId = randomDeckID,
                    key = randomCardKey,
                    value = randomCardValue,
                )

            // when
            val actual: MongoCard = card.toMongo()

            // then
            actual shouldBe expectedMongoCard
        }
    }

    @Nested
    @DisplayName("MongoCard.toCard()")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class MongoToCard {
        @Test
        fun `should map mongo to card if id is null`() {
            // given
            val mongoCard =
                MongoCard(
                    deckId = randomDeckID,
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

            // when
            val actual = mongoCard.toCardEntity()

            // then
            actual shouldBe expectedCard

            actual.id shouldBe null
        }

        @Test
        fun `should map mongo to card if id is NOT null`() {
            // given
            val mongoCard =
                MongoCard(
                    id = randomCardID,
                    deckId = randomDeckID,
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

            // when
            val actual = mongoCard.toCardEntity()

            // then
            actual shouldBe expectedCard

            actual.id shouldNotBe null
        }
    }
}
