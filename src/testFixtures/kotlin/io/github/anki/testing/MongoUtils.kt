package io.github.anki.testing

import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.DeckRepository
import io.github.anki.anki.repository.mongodb.document.MongoCard
import io.github.anki.anki.repository.mongodb.document.MongoDeck
import org.bson.types.ObjectId

fun DeckRepository.insertRandom(numberOfDecks: Int, userId: ObjectId): List<MongoDeck> {
    val listOfDecks: MutableCollection<MongoDeck> = mutableListOf()
    val prefix = "initial"
    repeat(numberOfDecks) {
        listOfDecks.add(
            MongoDeck(
                userId = userId,
                name = getRandomString(prefix),
                description = getRandomString(prefix),
            ),
        )
    }
    return this.insert(listOfDecks).get()
}

fun CardRepository.insertRandom(numberOfCards: Int, deckId: ObjectId): List<MongoCard> {
    val listOfCards: MutableCollection<MongoCard> = mutableListOf()
    val prefix = "initial"
    repeat(numberOfCards) {
        listOfCards.add(
            MongoCard(
                deckId = deckId,
                key = getRandomString(prefix),
                value = getRandomString(prefix),
            ),
        )
    }
    return this.insert(listOfCards).get()
}
