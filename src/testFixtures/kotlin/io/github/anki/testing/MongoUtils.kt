package io.github.anki.testing

import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.DeckRepository
import io.github.anki.anki.repository.mongodb.document.MongoCard
import io.github.anki.anki.repository.mongodb.document.MongoDeck
import org.bson.types.ObjectId

fun insertRandomDecks(deckRepository: DeckRepository, numberOfDecks: Int, userId: ObjectId): List<MongoDeck> {
    val listOfDecks: MutableList<MongoDeck> = mutableListOf()
    while (listOfDecks.size != numberOfDecks) {
        listOfDecks.add(
            MongoDeck(
                userId = userId,
                name = getRandomString(),
                description = getRandomString(),
            )
        )
    }
    return deckRepository.insert(listOfDecks)
}

fun insertRandomCards(cardRepository: CardRepository, numberOfCards: Int, deckId: ObjectId): List<MongoCard> {
    val listOfCards: MutableList<MongoCard> = mutableListOf()
    while (listOfCards.size != numberOfCards) {
        listOfCards.add(
            MongoCard(
                deckId = deckId,
                cardKey = getRandomString(),
                cardValue = getRandomString(),
            )
        )
    }
    return cardRepository.insert(listOfCards)
}

