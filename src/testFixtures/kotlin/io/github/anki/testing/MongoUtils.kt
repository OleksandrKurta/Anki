package io.github.anki.testing

import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.DeckRepository
import io.github.anki.anki.repository.mongodb.UserRepository
import io.github.anki.anki.repository.mongodb.document.MongoCard
import io.github.anki.anki.repository.mongodb.document.MongoDeck
import io.github.anki.anki.repository.mongodb.document.MongoUser
import org.bson.types.ObjectId
import reactor.core.publisher.Flux

fun DeckRepository.insertRandom(numberOfDecks: Int, userId: ObjectId): Flux<MongoDeck> =
    Flux
        .generate<MongoDeck> { sink ->
            sink.next(
                MongoDeck(
                    userId = userId,
                    name = getRandomString(DATA_PREFIX),
                    description = getRandomString(DATA_PREFIX),
                ),
            )
        }
        .take(numberOfDecks.toLong())
        .buffer(INSERT_BUFFER_SIZE)
        .flatMap(this::insert)

fun CardRepository.insertRandom(numberOfCards: Int, deckId: ObjectId): Flux<MongoCard> =
    Flux
        .generate<MongoCard> { sink ->
            sink.next(
                MongoCard(
                    deckId = deckId,
                    key = getRandomString(DATA_PREFIX),
                    value = getRandomString(DATA_PREFIX),
                ),
            )
        }
        .take(numberOfCards.toLong())
        .buffer(INSERT_BUFFER_SIZE)
        .flatMap(this::insert)

fun UserRepository.insertRandom(numberOfUsers: Int): Flux<MongoUser> =
    Flux
        .generate<MongoUser> { sink ->
            sink.next(
                MongoUser(
                    userName = getRandomString(DATA_PREFIX),
                    email = getRandomEmail(DATA_PREFIX),
                    password = getRandomString(DATA_PREFIX),
                ),
            )
        }
        .take(numberOfUsers.toLong())
        .buffer(INSERT_BUFFER_SIZE)
        .flatMap(this::insert)
