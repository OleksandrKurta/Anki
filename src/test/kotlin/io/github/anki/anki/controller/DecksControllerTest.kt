package io.github.anki.anki.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.anki.anki.controller.DecksController.Companion.BASE_URL
import io.github.anki.anki.controller.DecksController.Companion.CONCRETE_DECK
import io.github.anki.anki.controller.dto.DeckDtoResponse
import io.github.anki.anki.controller.dto.NewDeckRequest
import io.github.anki.anki.controller.dto.PatchDeckRequest
import io.github.anki.anki.controller.dto.auth.SignUpRequestDto
import io.github.anki.anki.controller.dto.mapper.toDeck
import io.github.anki.anki.controller.dto.mapper.toDto
import io.github.anki.anki.controller.dto.mapper.toUser
import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.DeckRepository
import io.github.anki.anki.repository.mongodb.UserRepository
import io.github.anki.anki.repository.mongodb.document.DocumentStatus
import io.github.anki.anki.service.exceptions.DeckDoesNotExistException
import io.github.anki.anki.service.model.mapper.toDeck
import io.github.anki.anki.service.model.mapper.toMongo
import io.github.anki.anki.service.model.mapper.toMongoUser
import io.github.anki.anki.service.secure.SecurityService
import io.github.anki.anki.service.secure.jwt.AuthTokenFilter.Companion.AUTH_HEADER_NAME
import io.github.anki.anki.service.secure.jwt.AuthTokenFilter.Companion.TOKEN_PREFIX
import io.github.anki.testing.MVCTest
import io.github.anki.testing.getRandomID
import io.github.anki.testing.getRandomString
import io.github.anki.testing.insertRandom
import io.github.anki.testing.testcontainers.TestContainersFactory
import io.github.anki.testing.testcontainers.with
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.bson.types.ObjectId
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import java.util.stream.Stream
import kotlin.test.BeforeTest
import kotlin.test.Test

@MVCTest
class DecksControllerTest @Autowired constructor(
    val mockMvc: MockMvc,
    val objectMapper: ObjectMapper,
    val deckRepository: DeckRepository,
    val cardRepository: CardRepository,
    val userRepository: UserRepository,
    val securityService: SecurityService,
    val authenticationManager: AuthenticationManager,
) {
    private lateinit var newDeckRequest: NewDeckRequest
    private lateinit var token: String

    @BeforeTest
    fun setUp() {
        newDeckRequest =
            NewDeckRequest(
                name = getRandomString("initial"),
                description = getRandomString("initial"),
            )

        val userDto =
            SignUpRequestDto(
                email = "${getRandomString()}@gmail.com",
                userName = getRandomString(),
                password = getRandomString(),
                roles = setOf(),
            )
        val user = userDto.toUser(securityService.encoder.encode(userDto.password))
        userRepository.insert(user.toMongoUser()).id.toString()
        val authentication: Authentication =
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(user.userName, userDto.password),
            )
        SecurityContextHolder.getContext().setAuthentication(authentication)
        token = securityService.jwtUtils.generateJwtToken(authentication)
    }

    @Nested
    @DisplayName("POST ${BASE_URL}")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class PostDeck {
        @Test
        fun `should create new Deck`() {
            // when
            val performPost = postNewDeck(newDeckRequest)

            val createdDeck =
                performPost.andReturn()
                    .response
                    .contentAsString
                    .let { objectMapper.readValue(it, DeckDtoResponse::class.java) }

            // then
            performPost
                .andDo { print() }
                .andExpect {
                    status { isCreated() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                }

            createdDeck.name shouldBe newDeckRequest.name
            createdDeck.description shouldBe newDeckRequest.description

            val deckFromMongo = deckRepository.findById(ObjectId(createdDeck.id)).get()!!

            createdDeck shouldBe deckFromMongo.toDeck().toDto()
        }

        @ParameterizedTest
        @MethodSource("invalidNewDeckRequestProvider")
        fun `should be error if deck name is not valid`(nameValue: String?) {
            // given
            newDeckRequest =
                NewDeckRequest(
                    name = nameValue,
                    description = getRandomString(),
                )

            // when
            val performPost = postNewDeck(newDeckRequest)

            // then
            performPost
                .andDo { print() }
                .andExpect {
                    status { isBadRequest() }
                    content {
                        contentType(MediaType.APPLICATION_JSON)
                        json("{\"name\": \"must not be blank\"}")
                    }
                }
        }

        private fun postNewDeck(newDeck: NewDeckRequest): ResultActionsDsl =
            mockMvc.post(BASE_URL) {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(newDeck)
                headers {
                    header(AUTH_HEADER_NAME, TOKEN_PREFIX + token)
                }
            }

        @Suppress("UnusedPrivateMember")
        private fun invalidNewDeckRequestProvider(): Stream<Arguments> =
            Stream.of(
                Arguments.of(null),
                Arguments.of(""),
            )
    }

    @Nested
    @DisplayName("GET ${BASE_URL}")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class GetDecks {
        @Test
        fun `should return all decks if they exist`() {
            // given
            val userId = securityService.jwtUtils.getUserIdFromJwtToken(token)
            val numberOfRandomCards = (5..100).random()
            val insertedDecks = deckRepository.insertRandom(numberOfRandomCards, ObjectId(userId))

            // when
            val performGet = sendGetDecks()

            // then
            val result =
                performGet
                    .andExpect {
                        status { isOk() }
                        content { contentType(MediaType.APPLICATION_JSON) }
                    }
                    .andReturn()

            val actualDecks: List<DeckDtoResponse> = objectMapper.readValue(result.response.contentAsString)

            actualDecks shouldContainExactlyInAnyOrder insertedDecks.map { it.toDeck().toDto() }
        }

        private fun sendGetDecks(): ResultActionsDsl =
            mockMvc.get(BASE_URL) {
                contentType = MediaType.APPLICATION_JSON
                headers {
                    header(AUTH_HEADER_NAME, TOKEN_PREFIX + token)
                }
            }
    }

    @Nested
    @DisplayName("PATCH ${BASE_URL}${CONCRETE_DECK}")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class PatchDeck {

        private val patchBaseUrl = BASE_URL + CONCRETE_DECK

        @Test
        fun `should patch deck if it exists`() {
            // given
            val userId = securityService.jwtUtils.getUserIdFromJwtToken(token)
            val insertedDeck = deckRepository.insertRandom(1, ObjectId(userId)).first()

            val patchDeckRequest =
                PatchDeckRequest(
                    name = getRandomString("updated"),
                    description = getRandomString("updated"),
                )

            // when
            val patchDeckResponse =
                sendPatchDeck(insertedDeck.id.toString(), patchDeckRequest)
                    .andExpect {
                        status { isOk() }
                        content { contentType(MediaType.APPLICATION_JSON) }
                    }
                    .andReturn()

            val actualDeck: DeckDtoResponse = objectMapper.readValue(patchDeckResponse.response.contentAsString)

            // then
            actualDeck.name shouldBe patchDeckRequest.name
            actualDeck.description shouldBe patchDeckRequest.description

            val deckFromMongo = deckRepository.findById(insertedDeck.id!!).get()!!

            deckFromMongo.name shouldBe patchDeckRequest.name

            deckFromMongo.description shouldBe patchDeckRequest.description
        }

        @Test
        fun `should return 400 if it does not exist`() {
            // given
            val notExistingDeckID = getRandomID().toString()

            // when
            val performPatch = sendPatchDeck(notExistingDeckID, PatchDeckRequest())

            // then
            val result =
                performPatch
                    .andExpect {
                        status { isBadRequest() }
                    }
                    .andReturn()

            val userId = securityService.jwtUtils.getUserIdFromJwtToken(token)
            result.response.contentAsString shouldBe
                DeckDoesNotExistException.fromDeckIdAndUserId(notExistingDeckID, userId).message

            deckRepository.existsById(ObjectId(notExistingDeckID)).get() shouldBe false
        }

        private fun sendPatchDeck(deckId: String, patchDeckRequest: PatchDeckRequest): ResultActionsDsl =
            mockMvc.patch(patchBaseUrl, deckId) {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(patchDeckRequest)
                headers {
                    header(AUTH_HEADER_NAME, TOKEN_PREFIX + token)
                }
            }
    }

    @Nested
    @DisplayName("DELETE ${BASE_URL}${CONCRETE_DECK}")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class DeleteDeck {

        private val deleteBaseUrl = BASE_URL + CONCRETE_DECK

        @Test
        fun `should delete the deck`() {
            // given
            val insertedDeck = deckRepository.insert(newDeckRequest.toDeck(mockUserId).toMongo()).get()
            val userId = securityService.jwtUtils.getUserIdFromJwtToken(token)
            val insertedCards = cardRepository.insertRandom((5..100).random(), insertedDeck.id!!)

            // when
            val performDelete = sendDeleteDeck(insertedDeck.id!!.toString())
            // then
            val result =
                performDelete
                    .andExpect {
                        status { isNoContent() }
                    }
                    .andReturn()

            result.response.contentType shouldBe null

            result.response.contentAsString.isEmpty() shouldBe true

            deckRepository.existsByIdWithStatus(insertedDeck.id!!, DocumentStatus.ACTIVE).get() shouldBe false
            deckRepository.existsByIdWithStatus(insertedDeck.id!!, DocumentStatus.DELETED).get() shouldBe true

            cardRepository.findByDeckIdWithStatus(insertedDeck.id!!).get().isEmpty() shouldBe true

            cardRepository.findByDeckIdWithStatus(
                insertedDeck.id!!, DocumentStatus.DELETED,
            ).get().size shouldBe insertedCards.size
        }

        private fun sendDeleteDeck(deckId: String): ResultActionsDsl =
            mockMvc.delete(deleteBaseUrl, deckId) {
                headers {
                    header(AUTH_HEADER_NAME, TOKEN_PREFIX + token)
                }
            }
    }

    companion object {
        @Container
        @Suppress("PropertyName")
        private val mongoDBContainer: MongoDBContainer = TestContainersFactory.newMongoContainer()

        @DynamicPropertySource
        @JvmStatic
        fun setMongoUri(registry: DynamicPropertyRegistry) {
            registry.with(mongoDBContainer)
        }
    }
}
