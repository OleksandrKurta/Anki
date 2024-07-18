package io.github.anki.anki.controllers
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.anki.anki.models.Card
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.junit.jupiter.api.TestInstance.Lifecycle
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

    val baseUrl = "/card"

    @Nested
    @DisplayName("GET /card/{id}")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class GetCard {
        @Test
        fun `should return concrete card`() {
            mockMvc.get("$baseUrl/0")
                    .andDo{ print() }
                    .andExpect { status { isOk() }
                                 jsonPath("$.id") { value("0")
                                 content { contentType(MediaType.APPLICATION_JSON) }
                    }
            }
        }

        @Test
        fun `should return null if not found`() {
            mockMvc.get("$baseUrl/3")
                .andDo{ print() }
                .andExpect { status { isNotFound() } }
        }
    }

    @Nested
    @DisplayName("POST /card")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class PostCard {

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

        @Test
        fun `should return BAD REQUEST if already exists`() {
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
    }

    @Nested
    @DisplayName("PATCH /card")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class PatchExistingBank {

        @Test
        fun `should update an existing bank`() {
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

        @Test
        fun `should return BAD REQUEST if no bank with given account number exists`() {
            // given
            val invalidBank = Card(550, 0, "key0", "value0")

            // when
            val performPatchRequest = mockMvc.patch(baseUrl) {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(invalidBank)
            }

            // then
            performPatchRequest
                .andDo { print() }
                .andExpect { status { isNotFound() } }
        }
    }


    @Nested
    @DisplayName("DELETE /card/{id}")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class DeleteExistingCard {

        @Test
        @DirtiesContext
        fun `should delete the card with the given id`() {
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

        @Test
        fun `should return NOT FOUND if no bank with given account number exists`() {
            // given
            val invalidId = 550

            // when/then
            mockMvc.delete("$baseUrl/$invalidId")
                .andDo { print() }
                .andExpect { status { isNotFound() } }
        }
    }
}
