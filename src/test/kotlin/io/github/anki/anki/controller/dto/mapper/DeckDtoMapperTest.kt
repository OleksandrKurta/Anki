package io.github.anki.anki.controller.dto.mapper

import io.github.anki.testing.getRandomID
import io.github.anki.testing.getRandomString
import org.bson.types.ObjectId
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import kotlin.test.BeforeTest
import kotlin.test.Test

class DeckDtoMapperTest {

    private lateinit var randomUserID: ObjectId
    private lateinit var randomDeckID: ObjectId
    private lateinit var randomDeckName: String
    private lateinit var randomDeckDescription: String

    @BeforeTest
    fun setUp() {
        randomUserID = getRandomID()
        randomDeckID = getRandomID()
        randomDeckName = getRandomString()
        randomDeckDescription = getRandomString()
    }

    @Nested
    @DisplayName("NewDeckRequest.toCollection()")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class NewDeckRequestToDeck {
        @Test
        fun `should `() {
            //given


            //when


            //then

        }
    }

}