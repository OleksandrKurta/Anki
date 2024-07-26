package io.github.anki.anki.service.model.mapper

import io.github.anki.anki.repository.mongodb.document.MongoCard
import io.github.anki.anki.service.model.Card
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test

class CardMapperKtTest {

    @Test
    fun `should map card to mongo when id is null`() {
        // GIVEN
        val card = Card(
            deckId = "66a166c38a31e37947a0d5ae",
            cardKey = "cardKey",
            cardValue = "cardValue"
        )
        val expectedMongoCard = MongoCard(
            deckId = ObjectId("66a166c38a31e37947a0d5ae"),
            cardKey = "cardKey",
            cardValue = "cardValue"
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
        val card = Card(
            id = "66a1678919893744c4b30a33",
            deckId = "66a166c38a31e37947a0d5ae",
            cardKey = "cardKey",
            cardValue = "cardValue"
        )
        val expectedMongoCard = MongoCard(
            id = ObjectId("66a1678919893744c4b30a33"),
            deckId = ObjectId("66a166c38a31e37947a0d5ae"),
            cardKey = "cardKey",
            cardValue = "cardValue"
        )

        // WHEN
        val actual: MongoCard = card.toMongo()

        // THEN
        actual shouldBe expectedMongoCard
    }
}
