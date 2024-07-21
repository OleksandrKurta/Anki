package io.github.anki.anki.controller
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.anki.anki.controller.model.CardDto
import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.model.MongoCard
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.delete

@SpringBootTest
@AutoConfigureMockMvc
class CardsControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val dataSource: CardRepository,
) {

    val baseUrl = ("/api/v1/cards")

    @DisplayName("GET /api/v1/cards/collection/{id}")
    @Test
    fun `should get concrete card`() {
        val newCard = CardDto(parentCollectionId = "123", cardKey = "newkey", cardValue ="newvalue")
        dataSource.insert(newCard.toModel())
        mockMvc.get("$baseUrl/collections/${newCard.parentCollectionId}")
                .andDo{ print() }
                .andExpect { status { isOk() }
                             jsonPath("$.id") { value("0")
                             content { contentType(MediaType.APPLICATION_JSON) }
                }
        }
    }

    @DisplayName("GET /api/v1/cards/collection/{id} not found")
    @Test
    fun `should get null if not found`() {
        // given
        val newCard = CardDto(parentCollectionId = "321", cardKey = "newkey", cardValue ="newvalue")

        mockMvc.get("$baseUrl/collections/${newCard.parentCollectionId}")
            .andDo{ print() }
            .andExpect { status { isNotFound() } }
    }


    @DisplayName("POST /api/v1/cards")
    @Test
    fun `should post card`() {
        val newCard = CardDto(parentCollectionId = "21", cardKey = "newkey", cardValue ="newvalue")

        val performPost = mockMvc.post(baseUrl) {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(newCard)
        }

        performPost
            .andDo { print() }
            .andExpect {
                status { isCreated() }
                content {
                    contentType(MediaType.APPLICATION_JSON)
                    json(objectMapper.writeValueAsString(newCard))
                }
            }

        mockMvc.get("$baseUrl/collection/${newCard.parentCollectionId}")
            .andExpect { content { json(objectMapper.writeValueAsString(newCard)) } }


    }
    @DisplayName("POST /api/v1/cards already exists")
    @Test
    fun `should get BAD REQUEST when already exists`() {
        //given
        val nonExistingCard = CardDto("0", "key2", "value2")
        val card: CardDto = dataSource.insert(nonExistingCard.toModel()).toDto()

        // when
        val existingCard = CardDto( "0", "key2", "value2")
        existingCard.id = card.id
        val performPostExisting = mockMvc.post(baseUrl) {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(existingCard)
        }

        // then
        performPostExisting
            .andDo { print() }
            .andExpect { status { isBadRequest() } }
    }

    @DisplayName("PATCH /api/v1/cards")
    @Test
    fun `should update an existing card`() {
        // given
        val nonExistingCard = CardDto("1111", "key2", "value2")
        val card: CardDto = dataSource.insert(nonExistingCard.toModel()).toDto()

        // when
        val updatedCard = CardDto("1111", "key3", "value3")
        updatedCard.id = card.id

        val performPatchRequest = mockMvc.patch(baseUrl) {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(updatedCard)
        }

        // then
        performPatchRequest
            .andDo { print() }
            .andExpect {
                status { isOk() }
                content {
                    contentType(MediaType.APPLICATION_JSON)
                    json(objectMapper.writeValueAsString(updatedCard))
                }
            }

        mockMvc.get("$baseUrl/${updatedCard.id}")
            .andExpect { content { json(objectMapper.writeValueAsString(updatedCard)) } }
    }

    @DisplayName("PATCH /api/v1/cards")
    @Test
    fun `should get BAD REQUEST when card not exists`() {
        // given
        val invalidIdCard = MongoCard("111", "key2", "value2")

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
        val dto = CardDto("22", "key2", "value2")
        val model = dataSource.insert(dto.toModel())

        // when/then
        mockMvc.delete("$baseUrl/$model.id")
            .andDo { print() }
            .andExpect {
                status { isNoContent() }
            }

        mockMvc.get("$baseUrl/collection/${dto.parentCollectionId}")
            .andExpect { status { isNotFound() } }
    }

    @DisplayName("DELETE api/v1/cards/{id}")
    @Test
    fun `should get NOT FOUND when no card exists`() {
        // given
        val invalidIdCard = CardDto( "0", "key2", "value2")
        dataSource.deleteById(invalidIdCard.toModel().id)

        // when/then
        mockMvc.delete("$baseUrl/$invalidIdCard.id")
            .andDo { print() }
            .andExpect { status { isNotFound() } }
    }

}
