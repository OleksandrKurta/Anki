package io.github.anki.anki.service

import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.DeckRepository
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class CardsServiceTest {

    @MockK
    lateinit var deckRepository: DeckRepository

    @MockK
    lateinit var cardRepository: CardRepository

    @InjectMockKs
    lateinit var sut: DeckService

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }
}
