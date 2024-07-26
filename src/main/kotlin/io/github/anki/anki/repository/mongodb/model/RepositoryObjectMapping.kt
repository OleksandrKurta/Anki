package io.github.anki.anki.repository.mongodb.model
import io.github.anki.anki.service.model.Card


fun mapRepositoryToService(repository: MongoCard): Card {
        return Card(repository.id.toString(),
                repository.parentCollectionId,
                repository.cardKey,
                repository.cardValue)
}
