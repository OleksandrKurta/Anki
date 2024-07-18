package io.github.anki.anki.controller
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.anki.anki.models.Card
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
) {

    val baseUrl = ("/api/v1/cards")

    @DisplayName("GET /api/v1/cards/{id}")
    @Test
    fun `should get concrete card`() {
        mockMvc.get("$baseUrl/0")
                .andDo{ print() }
                .andExpect { status { isOk() }
                             jsonPath("$.id") { value("0")
                             content { contentType(MediaType.APPLICATION_JSON) }
                }
        }
    }

    @DisplayName("GET /api/v1/cards/{id} not found")
    @Test
    fun `should get null if not found`() {
        mockMvc.get("$baseUrl/3")
            .andDo{ print() }
            .andExpect { status { isNotFound() } }
    }


    @DisplayName("POST /api/v1/cards")
    @Test
    fun `should post card`() {
        val newCard = Card(id=3, parentCollectionId = 0, key="newkey", value="newvalue")

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

        mockMvc.get("$baseUrl/${newCard.id}")
            .andExpect { content { json(objectMapper.writeValueAsString(newCard)) } }


    }
    @DisplayName("POST /api/v1/cards already exists")
    @Test
    fun `should get BAD REQUEST when already exists`() {
        // given
        val existingCard = Card(1, 0, "key2", "value2")

        // when
        val performPost = mockMvc.post(baseUrl) {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(existingCard)
        }

        // then
        performPost
            .andDo { print() }
            .andExpect { status { isBadRequest() } }
    }

    @DisplayName("PATCH /api/v1/cards")
    @Test
    fun `should update an existing card`() {
        // given
        val updatedCard = Card(0, 0, "key0", "value0")

        // when
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
    fun `should get BAD REQUEST when card exists`() {
        // given
        val invalidCard = Card(550, 0, "key0", "value0")

        // when
        val performPatchRequest = mockMvc.patch(baseUrl) {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(invalidCard)
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
        val givenId = 0

        // when/then
        mockMvc.delete("$baseUrl/$givenId")
            .andDo { print() }
            .andExpect {
                status { isNoContent() }
            }

        mockMvc.get("$baseUrl/$givenId")
            .andExpect { status { isNotFound() } }
    }

    @DisplayName("DELETE api/v1/cards/{id}")
    @Test
    fun `should get NOT FOUND when no card exists`() {
        // given
        val invalidId = 550

        // when/then
        mockMvc.delete("$baseUrl/$invalidId")
            .andDo { print() }
            .andExpect { status { isNotFound() } }
    }

}
