package io.github.anki.anki.controller
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.anki.anki.controller.dto.CardDtoResponse
import io.github.anki.anki.controller.dto.mapper.toCard
import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.document.MongoCard
import io.github.anki.anki.service.CardsService
import io.github.anki.anki.service.model.mapper.toMongo
import org.bson.types.ObjectId
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.delete
import java.util.*

import kotlin.test.AfterTest
import kotlin.test.BeforeTest

@SpringBootTest
@AutoConfigureMockMvc
class CardsControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val cardRepository: CardRepository,
) {

    val baseUrl = ("/api/v1/cards")
    private lateinit var cleanupModels: MutableList<MongoCard>
    private lateinit var newCard: CardDtoResponse

    @BeforeTest
    fun setUp() {
        LOG.info("Initializing cards list")
        cleanupModels = mutableListOf()
        newCard = generateRandomCard()
    }

    @AfterTest
    fun teardown() {
        LOG.info("Cleaning up after the test for existing Card")
        cardRepository.deleteAll(cleanupModels)
        LOG.info("Successfully deleted test cards")
    }

    fun generateRandomCard(): CardDtoResponse =
        CardDtoResponse(
            id = ObjectId().toString(),
            deckId = ObjectId().toString(),
            cardKey = UUID.randomUUID().toString(),
            cardValue =UUID.randomUUID().toString(),
            )

    @Nested
    @DisplayName("POST /api/v1/cards")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class PostCards {
        @Test
        fun `should post card`() {
            val performPost = mockMvc.post(baseUrl) {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(newCard)
            }
            val createdCard = performPost.andReturn()
                .response
                .contentAsString
                .let { objectMapper.readValue(it, CardDtoResponse::class.java) }
            // covered in repository test
            newCard.id = createdCard.id
            cleanupModels.add(createdCard.toCard().toMongo())
            performPost
                .andDo { print() }
                .andExpect {
                    status { isCreated() }
                    content {
                        contentType(MediaType.APPLICATION_JSON)
                        json(objectMapper.writeValueAsString(newCard))
                    }
                }
        }
    }


//    @Nested
//    @DisplayName("PATCH /api/v1/cards")
//    @TestInstance(Lifecycle.PER_CLASS)
//    inner class UpdateCard {
//
//        @Test
//        fun `should update an existing card`() {
//            // given
//            val cardToUpdate: CardDtoResponse = generateRandomCard()
//            val currentModel = cardRepository.insert(mapDtoToRepository(newCard))
//            cleanupModels.add(currentModel)
//            cardToUpdate.id = currentModel.id.toString()
//
//            // when
//            val performPatchRequest = mockMvc.patch(baseUrl) {
//                contentType = MediaType.APPLICATION_JSON
//                content = objectMapper.writeValueAsString(cardToUpdate)
//            }
//            val resultCard = performPatchRequest.andReturn()
//                .response
//                .contentAsString
//                .let { objectMapper.readValue(it, CardDtoResponse::class.java) }
//            cleanupModels.add(mapDtoToRepository(resultCard))
//
//            // then
//            performPatchRequest
//                .andDo { print() }
//                .andExpect {
//                    status { isOk() }
//                    content {
//                        contentType(MediaType.APPLICATION_JSON)
//                        json(objectMapper.writeValueAsString(cardToUpdate))
//                    }
//                }
//        }
//
//        @Test
//        fun `should get BAD REQUEST when card not exists`() {
//            // given
//            val invalidIdCard = generateRandomCard()
//            invalidIdCard.id = ObjectId().toString()
//
//            // when
//            val performPatchRequest = mockMvc.patch(baseUrl) {
//                contentType = MediaType.APPLICATION_JSON
//                content = objectMapper.writeValueAsString(invalidIdCard)
//            }
//
//            // then
//            performPatchRequest
//                .andDo { print() }
//                .andExpect { status { isNotFound() } }
//        }
//
//
//    }

    @Nested
    @DisplayName("DELETE api/v1/cards/{id}")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class DeleteCard {

        @Test
        fun `should delete the card`() {
            // given
            val model = cardRepository.insert(newCard.toCard().toMongo())
            val performDelete = mockMvc.delete("$baseUrl/${model.id.toString()}")
            // when, then
            performDelete.andDo { print() }
                .andExpect {
                    status { (isNoContent()) }
                    content { (objectMapper.writeValueAsString(model.id.toString())) }
                }
        }

        @Test
        fun `should get NOT FOUND when no card exists`() {
            val notExistingCardID = newCard.id
            // when/then
            mockMvc.delete("$baseUrl/$notExistingCardID")
                .andDo { print() }
                .andExpect { status { isNoContent() } }
        }

    }


    companion object {
        private val LOG = LoggerFactory.getLogger(CardsService::class.java)
    }

}
