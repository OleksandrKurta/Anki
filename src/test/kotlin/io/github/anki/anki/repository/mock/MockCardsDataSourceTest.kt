package io.github.anki.anki.repository.mock


import io.github.anki.anki.models.Card
import org.junit.jupiter.api.Test
import org.springframework.util.Assert

class MockCardsDataSourceTest {
    private val mockDataSource = MockCardsDataSource()

    @Test
    fun `should provide some mock data`() {
        // when
        val card: Card = mockDataSource.retrieveCard(0)

        // then
        Assert.notNull(card, "Should not be null")
    }
}
