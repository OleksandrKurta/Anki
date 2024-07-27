package io.github.anki.anki.service.model.mapper

import io.github.anki.anki.repository.mongodb.document.MongoCard
import io.github.anki.anki.service.CardsService
import io.github.anki.anki.service.model.Card
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.shouldBe
import org.bson.types.ObjectId
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.slf4j.LoggerFactory
import java.util.UUID
import kotlin.test.BeforeTest

class CardMapperKtTest {

    private lateinit var randomCardID: ObjectId
    private lateinit var randomDeckID: ObjectId
    private lateinit var randomCardKey: String
    private lateinit var randomCardValue: String

    private fun getRandomID(): ObjectId =
        ObjectId.get().also { LOG.info("Generating random ObjectId {}", it) }

    private fun getRandomString(): String =
        UUID.randomUUID().toString().also { LOG.info("Generating random String {}", it) }

    @BeforeTest
    fun setUp() {
        randomCardID = getRandomID()
        randomDeckID = getRandomID()
        randomCardKey = getRandomString()
        randomCardValue = getRandomString()
    }

    @Nested
    @DisplayName("Card.toMongo()")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class CardToMongo {
        @Test
        fun `should map card to mongo when id is null`() {
            // GIVEN
            val card = Card(
                deckId = randomDeckID.toString(),
                cardKey = randomCardKey,
                cardValue = randomCardValue,
            )
            val expectedMongoCard = MongoCard(
                deckId = randomDeckID,
                cardKey = randomCardKey,
                cardValue = randomCardValue,
            )

            // WHEN
            val actual: MongoCard = card.toMongo()

            // THEN
            actual.shouldBeEqualToIgnoringFields(expectedMongoCard, MongoCard::id)

            actual.id shouldBe null
        }

        @Test
        fun `should map card to mongo when id is not null`() {
            // GIVEN
            val card = Card(
                id = randomCardID.toString(),
                deckId = randomDeckID.toString(),
                cardKey = randomCardKey,
                cardValue = randomCardValue,
            )
            val expectedMongoCard = MongoCard(
                id = randomCardID,
                deckId = randomDeckID,
                cardKey = randomCardKey,
                cardValue = randomCardValue,
            )

            // WHEN
            val actual: MongoCard = card.toMongo()

            // THEN
            actual shouldBe expectedMongoCard
        }

    }

    @Nested
    @DisplayName("MongoCard.toCard()")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class MongoToCard {
        @Test
        fun `should map mongo to card if id is null`() {
            //given
            val mongoCard = MongoCard(
                id = null,
                deckId = randomDeckID,
                cardKey = randomCardKey,
                cardValue = randomCardValue,
            )
            val expectedCard = Card(
                id = null,
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
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(CardsService::class.java)
    }
}
