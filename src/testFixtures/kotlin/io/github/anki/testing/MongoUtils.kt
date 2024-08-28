package io.github.anki.testing

import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.DeckRepository
import io.github.anki.anki.repository.mongodb.UserRepository
import io.github.anki.anki.repository.mongodb.document.MongoCard
import io.github.anki.anki.repository.mongodb.document.MongoDeck
import io.github.anki.anki.repository.mongodb.document.MongoUser
import org.bson.types.ObjectId

fun DeckRepository.insertRandom(numberOfDecks: Int, userId: ObjectId): List<MongoDeck> {
    val listOfDecks: MutableCollection<MongoDeck> = mutableListOf()
    val prefix = DATA_PREFIX
    repeat(numberOfDecks) {
        listOfDecks.add(
            MongoDeck(
                userId = userId,
                name = getRandomString(prefix),
                description = getRandomString(prefix),
            ),
        )
    }
    return this.insert(listOfDecks)
}

fun CardRepository.insertRandom(numberOfCards: Int, deckId: ObjectId): List<MongoCard> {
    val listOfCards: MutableCollection<MongoCard> = mutableListOf()
    val prefix = DATA_PREFIX
    repeat(numberOfCards) {
        listOfCards.add(
            MongoCard(
                deckId = deckId,
                key = getRandomString(prefix),
                value = getRandomString(prefix),
            ),
        )
    }
    return this.insert(listOfCards)
}

fun UserRepository.insertRandom(numberOfUsers: Int): List<MongoUser> {
    val listOfUsers: MutableCollection<MongoUser> = mutableListOf()
    val prefix = DATA_PREFIX
    repeat(numberOfUsers) {
        listOfUsers.add(
            MongoUser(
                userName = getRandomString(prefix),
                email = getRandomEmail(prefix),
                password = getRandomString(prefix),
            ),
        )
    }
    return this.insert(listOfUsers)
}
