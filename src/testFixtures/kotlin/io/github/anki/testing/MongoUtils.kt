package io.github.anki.testing

import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.DeckRepository
import io.github.anki.anki.repository.mongodb.document.MongoCard
import io.github.anki.anki.repository.mongodb.document.MongoDeck
import org.bson.types.ObjectId

fun DeckRepository.insertRandom(numberOfDecks: Int, userId: ObjectId): List<MongoDeck> {
    val listOfDecks: MutableCollection<MongoDeck> = mutableListOf()
    repeat(numberOfDecks) {
        listOfDecks.add(
            MongoDeck(
                userId = userId,
                name = getRandomString(),
                description = getRandomString(),
            )
        )
    }
    return this.insert(listOfDecks)
}

fun CardRepository.insertRandom(numberOfCards: Int, deckId: ObjectId): List<MongoCard> {
    val listOfCards: MutableCollection<MongoCard> = mutableListOf()
    repeat(numberOfCards) {
        listOfCards.add(
            MongoCard(
                deckId = deckId,
                cardKey = getRandomString(),
                cardValue = getRandomString(),
            )
        )
    }
    return this.insert(listOfCards)
}

