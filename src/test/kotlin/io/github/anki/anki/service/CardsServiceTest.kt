package io.github.anki.anki.service

import io.github.anki.anki.repository.CardsDataSource
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class CardsServiceTest {

    private val dataSource: CardsDataSource = mockk(relaxed = true)

    @Test
    fun `should fetch all cards`() {

        verify(exactly = 1) { dataSource.retrieveCard(0) }
    }
}
