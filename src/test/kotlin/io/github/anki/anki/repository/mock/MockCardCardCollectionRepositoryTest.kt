package io.github.anki.anki.repository.mock

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test


class MockCardCollectionRepositoryTest{

    private val mockCollectionRepository = MockCardCollectionRepository()

    @Test
    fun `should get collection`() {
        //when
        val collections = mockCollectionRepository.getCardCollections()

        //then
        Assertions.assertThat(collections).isNotEmpty

    }
}