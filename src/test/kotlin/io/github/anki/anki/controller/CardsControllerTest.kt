package io.github.anki.anki.controller
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.anki.anki.controller.model.CardDto
import io.github.anki.anki.controller.model.mapDtoToRepository
import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.model.MongoCard
import org.bson.types.ObjectId
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.delete
import java.util.*

import kotlin.test.AfterTest
import kotlin.test.BeforeTest

@SpringBootTest()
@AutoConfigureMockMvc
class CardsControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val dataSource: CardRepository,
) {

    val baseUrl = ("/api/v1/cards")
    private lateinit var cleanupModels: MutableList<MongoCard>
    private var newCard: CardDto? = null

    @BeforeTest
    fun setUp() {
        println("Initialized cards list")
        cleanupModels = mutableListOf()
        newCard = generateRandomCard()
    }

    @AfterTest
    fun teardown() {
        println("Cleaning up after the test for existing Card")
        for (model: MongoCard in cleanupModels)
            dataSource.deleteById(model.id.toString())
    }

    fun generateRandomCard(): CardDto = CardDto(
            parentCollectionId = UUID.randomUUID().toString(),
            cardKey = UUID.randomUUID().toString(),
            cardValue =UUID.randomUUID().toString())


    @DisplayName("POST /api/v1/cards")
    @Test
    fun `should post card`() {
        val performPost = mockMvc.post(baseUrl) {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(newCard)
        }
        val createdCard = performPost.andReturn()
            .response
            .contentAsString
            .let { objectMapper.readValue(it, CardDto::class.java) }
        // covered in repository test
        newCard?.id = createdCard.id
        cleanupModels.add(mapDtoToRepository(createdCard))
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


    @DisplayName("PATCH /api/v1/cards")
    @Test
    fun `should update an existing card`() {
        // given
        val cardToUpdate: CardDto = generateRandomCard()
        val currentModel = dataSource.insert(mapDtoToRepository(newCard))
        cleanupModels.add(currentModel)
        cardToUpdate.id = currentModel.id.toString()

        // when
        val performPatchRequest = mockMvc.patch(baseUrl) {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(cardToUpdate)
        }
        val resultCard = performPatchRequest.andReturn()
            .response
            .contentAsString
            .let { objectMapper.readValue(it, CardDto::class.java) }
        cleanupModels.add(mapDtoToRepository(resultCard))

        // then
        performPatchRequest
            .andDo { print() }
            .andExpect {
                status { isOk() }
                content {
                    contentType(MediaType.APPLICATION_JSON)
                    json(objectMapper.writeValueAsString(cardToUpdate))
                }
            }
    }

    @DisplayName("PATCH /api/v1/cards non existent card")
    @Test
    fun `should get BAD REQUEST when card not exists`() {
        // given
        val invalidIdCard = generateRandomCard()
        invalidIdCard.id = ObjectId().toString()

        // when
        val performPatchRequest = mockMvc.patch(baseUrl) {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(invalidIdCard)
        }

        // then
        performPatchRequest
            .andDo { print() }
            .andExpect { status { isNotFound() } }
    }


    @DisplayName("DELETE api/v1/cards/{id}")
    @Test
    @DirtiesContext
    fun `should delete the card`() {
        // given
        val model = dataSource.insert(mapDtoToRepository( newCard))
        val performDelete = mockMvc.delete("$baseUrl/${model.id.toString()}")
        // when, then
            performDelete.andDo { print() }
            .andExpect {
                status { isOk() }
                content { (objectMapper.writeValueAsString(model.id.toString())) }
            }
    }

    @DisplayName("DELETE api/v1/cards/{id}")
    @Test
    fun `should get NOT FOUND when no card exists`() {
        // given
        newCard?.id = ObjectId().toString()

        // when/then
        mockMvc.delete("$baseUrl/$newCard.id")
            .andDo { print() }
            .andExpect { status { isNotFound() } }
    }

}
