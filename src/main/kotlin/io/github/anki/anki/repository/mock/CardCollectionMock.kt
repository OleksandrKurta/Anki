package io.github.anki.anki.repository.mock

import io.github.anki.anki.models.CardCollection
import io.github.anki.anki.repository.CardCollectionRepository
import org.springframework.stereotype.Repository


@Repository
class MockCardCollectionRepository : CardCollectionRepository {

    override fun getCardCollections(): Collection<CardCollection> {
        TODO("Not yet implemented")
    }

}