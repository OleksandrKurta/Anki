package io.github.anki.anki.service.model.mapper

import io.github.anki.anki.repository.mongodb.document.MongoCard
import io.github.anki.anki.service.CardsService
import io.github.anki.anki.service.model.Card
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class CardMapperKtTest {

    private fun getRandomID(): ObjectId =
        ObjectId.get().also { LOG.info("Generating random ObjectId $it") }

    @Test
    fun `should map card to mongo when id is null`() {
        // GIVEN
        val randomID = getRandomID()
        val card =
            Card(
                deckId = randomID.toString(),
                cardKey = "cardKey",
                cardValue = "cardValue",
            )
        val expectedMongoCard =
            MongoCard(
                deckId = randomID,
                cardKey = "cardKey",
                cardValue = "cardValue",
            )

        // WHEN
        val actual: MongoCard = card.toMongo()

        // THEN
        actual.shouldBeEqualToIgnoringFields(expectedMongoCard, MongoCard::id)

        actual.id shouldNotBe null
    }

    @Test
    fun `should map card to mongo when id is not null`() {
        // GIVEN
        val randomCardID = getRandomID()
        val randomDeckId = getRandomID()
        val card =
            Card(
                id = randomCardID.toString(),
                deckId = randomDeckId.toString(),
                cardKey = "cardKey",
                cardValue = "cardValue",
            )
        val expectedMongoCard =
            MongoCard(
                id = randomCardID,
                deckId = randomDeckId,
                cardKey = "cardKey",
                cardValue = "cardValue",
            )

        // WHEN
        val actual: MongoCard = card.toMongo()

        // THEN
        actual shouldBe expectedMongoCard
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(CardsService::class.java)
    }
}
