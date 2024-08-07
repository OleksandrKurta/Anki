package io.github.anki.anki.service

import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.DeckRepository
import io.github.anki.anki.repository.mongodb.document.MongoDeck
import io.github.anki.anki.service.model.Deck
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class DeckServiceTest {

    @MockK
    lateinit var deckRepository: DeckRepository

    @MockK
    lateinit var cardRepository: CardRepository

    @InjectMockKs
    lateinit var sut: DeckService

    @Test
    fun `should create new deck always`() {
        // GIVEN
        val userId = ObjectId()
        val deck =
            Deck(
                userId = userId.toHexString(),
                name = "name",
                description = "description",
            )
        val mongoDeck =
            MongoDeck(
                userId = userId,
                name = deck.name,
                description = deck.description,
            )
        val createdMongoDeck = mongoDeck.copy(id = ObjectId())
        val expectedDeck = deck.copy(id = createdMongoDeck.id!!.toHexString())
        every { deckRepository.insert(mongoDeck) } returns createdMongoDeck

        // WHEN
        val actual: Deck = sut.createNewDeck(deck)

        // THEN
        actual shouldBe expectedDeck

        // AND
        verify(exactly = 1) {
            deckRepository.insert(mongoDeck)
        }
    }
}
