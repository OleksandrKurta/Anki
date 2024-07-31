package io.github.anki.anki.service.model.mapper

import io.github.anki.anki.repository.mongodb.document.MongoDeck
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

class DeckMapperTest {

    private lateinit var randomUserID: ObjectId
    private lateinit var randomDeckID: ObjectId
    private lateinit var randomDeckName: String
    private lateinit var randomDeckDescription: String

    @BeforeTest
    fun setUp() {
        randomUserID = getRandomID()
        randomDeckID = getRandomID()
        randomDeckName = getRandomString()
        randomDeckDescription = getRandomString()
    }

    @Nested
    @DisplayName("Deck.toMongo()")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class DeckToMongo {
        @Test
        fun `should map Deck to MongoDeck when id is null`() {
            //given
            val deck = Deck(
                id = null,
                userId = randomUserID.toString(),
                name = randomDeckName,
                description = randomDeckDescription,
            )
            val expectedMongoDeck = MongoDeck(
                id = null,
                userId = randomUserID,
                name = randomDeckName,
                description = randomDeckDescription,
            )

            //when
            val actualMongoDeck = deck.toMongo()

            //then
            actualMongoDeck shouldBe expectedMongoDeck

            actualMongoDeck.id shouldBe null

        }

        @Test
        fun `should map Deck to MongoDeck when id is NOT null`() {
            //given
            val deck = Deck(
                id = randomDeckID.toString(),
                userId = randomUserID.toString(),
                name = randomDeckName,
                description = randomDeckDescription,
            )
            val expectedMongoDeck = MongoDeck(
                id = randomDeckID,
                userId = randomUserID,
                name = randomDeckName,
                description = randomDeckDescription,
            )

            //when
            val actualMongoDeck = deck.toMongo()

            //then
            actualMongoDeck shouldBe expectedMongoDeck

        }
    }
}

