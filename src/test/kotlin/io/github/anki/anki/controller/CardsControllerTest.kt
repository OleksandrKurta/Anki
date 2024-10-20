package io.github.anki.anki.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.anki.anki.controller.CardsController.Companion.BASE_URL
import io.github.anki.anki.controller.CardsController.Companion.CONCRETE_CARD
import io.github.anki.anki.controller.dto.CardDtoResponse
import io.github.anki.anki.controller.dto.NewCardRequest
import io.github.anki.anki.controller.dto.PaginationDto
import io.github.anki.anki.controller.dto.PatchCardRequest
import io.github.anki.anki.controller.dto.auth.SignUpRequestDto
import io.github.anki.anki.controller.dto.mapper.toDto
import io.github.anki.anki.controller.dto.mapper.toUser
import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.DeckRepository
import io.github.anki.anki.repository.mongodb.UserRepository
import io.github.anki.anki.repository.mongodb.document.DocumentStatus
import io.github.anki.anki.repository.mongodb.document.MongoCard
import io.github.anki.anki.repository.mongodb.document.MongoDeck
import io.github.anki.anki.service.exceptions.DeckDoesNotExistException
import io.github.anki.anki.service.model.User
import io.github.anki.anki.service.model.mapper.toCard
import io.github.anki.anki.service.model.mapper.toMongoUser
import io.github.anki.anki.service.secure.SecurityService
import io.github.anki.anki.service.secure.jwt.AuthTokenFilter.Companion.AUTH_HEADER_NAME
import io.github.anki.anki.service.secure.jwt.AuthTokenFilter.Companion.TOKEN_PREFIX
import io.github.anki.anki.service.secure.jwt.JwtUtils
import io.github.anki.testing.DATA_PREFIX
import io.github.anki.testing.ReactiveIntegrationTest
import io.github.anki.testing.getAndMapResponseBody
import io.github.anki.testing.getRandomID
import io.github.anki.testing.getRandomString
import io.github.anki.testing.insertRandom
import io.github.anki.testing.randomUser
import io.github.anki.testing.testcontainers.TestContainersFactory
import io.github.anki.testing.testcontainers.with
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.bson.types.ObjectId
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import kotlin.test.BeforeTest

@ReactiveIntegrationTest
class CardsControllerTest @Autowired constructor(
    private val objectMapper: ObjectMapper,
    private val cardRepository: CardRepository,
    private val deckRepository: DeckRepository,
    private val userRepository: UserRepository,
    private val securityService: SecurityService,
    private val authenticationManager: ReactiveAuthenticationManager,
    private val encoder: PasswordEncoder,
    private val jwtUtil: JwtUtils,
    private val webTestClient: WebTestClient,
) {
    private lateinit var newCard: NewCardRequest
    private lateinit var insertedDeck: MongoDeck
    private lateinit var token: String

    @BeforeTest
    fun setUp() {
        val newUser = SignUpRequestDto.randomUser()
        val user: User = newUser.toUser(encoder.encode(newUser.password))
        val mongoUser = userRepository.insert(user.toMongoUser()).block()!!
        authenticationManager
            .authenticate(UsernamePasswordAuthenticationToken(user.userName, newUser.password))
            .then()
        token = jwtUtil.generateJwtToken(user)
        newCard =
            NewCardRequest(
                key = getRandomString(DATA_PREFIX),
                value = getRandomString(DATA_PREFIX),
            )
        insertedDeck = deckRepository.insertRandom(1, userId = mongoUser.id!!).blockFirst()!!
    }

    @Nested
    @DisplayName("POST ${BASE_URL}")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class PostCard {
        @Test
        fun `should create new Card always`() {
            // when
            val response = postNewCard(newCard, insertedDeck.id.toString())

            // then
            response
                .expectStatus()
                .isCreated

            val createdCard = response.getAndMapResponseBody<CardDtoResponse>()

            val cardFromMongo = cardRepository.findById(ObjectId(createdCard.id)).block()!!

            cardFromMongo shouldNotBe null

            createdCard shouldBe cardFromMongo.toCard().toDto()
        }

//        @Test
//        fun `should return 400 if deck does not exist`() {
//            // given
//            val randomDeckId = getRandomID()
//
//            // when
//            val performPost = postNewCard(newCard, randomDeckId.toString())
//            val result =
//                performPost
//                    .andDo { print() }
//                    .andExpect { status { isBadRequest() } }
//                    .andReturn()
//
//            // then
//            val userId = securityService.jwtUtils.getUserIdFromJwtToken(token)
//
//            result.response.contentAsString shouldBe
//                DeckDoesNotExistException.fromDeckIdAndUserId(randomDeckId.toString(), userId).message
//        }
//
//        @Test
//        fun `should return authException token was not in header`() {
//            // given
//            val randomDeckId = getRandomID()
//
//            // when
//            val performPost =
//                mockMvc.post(BASE_URL, randomDeckId.toString()) {
//                    contentType = MediaType.APPLICATION_JSON
//                    content = objectMapper.writeValueAsString(newCard)
//                }
//            val result =
//                performPost
//                    .andDo { print() }
//                    .andExpect { status { isUnauthorized() } }
//                    .andReturn()
//            // then
//            result.response.status shouldBe HttpStatus.UNAUTHORIZED.value()
//        }

        private fun postNewCard(newCard: NewCardRequest, deckId: String): WebTestClient.ResponseSpec =
            webTestClient
                .post()
                .uri(BASE_URL, deckId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(newCard))
                .header(AUTH_HEADER_NAME, TOKEN_PREFIX + token)
                .exchange()
    }

//    @Nested
//    @DisplayName("GET ${BASE_URL}")
//    @TestInstance(Lifecycle.PER_CLASS)
//    inner class GetCards {
//        @Test
//        fun `should return all cards if they exist`() {
//            // given
//            val mongoCards = cardRepository.insertRandom((5..100).random(), insertedDeck.id!!)
//
//            // when
//            val result = sendGetCards(insertedDeck.id!!.toString(), PaginationDto(limit = 100))
//
//            // then
//            result.andExpect {
//                status { isOk() }
//            }
//
//            val cardsFromResponse: List<CardDtoResponse> =
//                result
//                    .andReturn()
//                    .let { objectMapper.readValue(it.response.contentAsString) }
//
//            cardsFromResponse shouldBe mongoCards.map { it.toCard().toDto() }
//        }
//
//        @ParameterizedTest
//        @ValueSource(ints = [50, 75, 100, 228, 1488])
//        fun `should return cards with pagination`(cardsAmount: Int) {
//            // given
//            val mongoCards = cardRepository.insertRandom(cardsAmount, insertedDeck.id!!)
//
//            val cardsFromResponses = mutableListOf<CardDtoResponse>()
//
//            var requestCounter = 0
//
//            // when
//            do {
//                val paginationDto = PaginationDto(offset = cardsFromResponses.size)
//                val result = sendGetCards(deckId = insertedDeck.id!!.toString(), paginationDto = paginationDto)
//                result.andExpect {
//                    status { isOk() }
//                }
//                val cardsFromThisResponse: List<CardDtoResponse> =
//                    result.andReturn().let {
//                        objectMapper.readValue(it.response.contentAsString)
//                    }
//
//                cardsFromResponses.addAll(cardsFromThisResponse)
//                requestCounter++
//            } while (cardsFromThisResponse.size == paginationDto.limit)
//
//            // then
//            cardsFromResponses shouldBe mongoCards.map { it.toCard().toDto() }
//
//            requestCounter shouldBe cardsAmount / PaginationDto.DEFAULT_LIMIT + 1
//        }
//
//        private fun sendGetCards(deckId: String, paginationDto: PaginationDto): ResultActionsDsl =
//            mockMvc.get(BASE_URL, deckId) {
//                header(AUTH_HEADER_NAME, TOKEN_PREFIX + token)
//                param(PaginationDto.LIMIT, paginationDto.limit.toString())
//                param(PaginationDto.OFFSET, paginationDto.offset.toString())
//            }.andDo { print() }
//    }
//
//    @Nested
//    @DisplayName("PATCH ${BASE_URL}")
//    @TestInstance(Lifecycle.PER_CLASS)
//    inner class PatchCard {
//        private val patchBaseUrl = BASE_URL + CONCRETE_CARD
//        private lateinit var insertedCard: MongoCard
//
//        @BeforeTest
//        fun createCard() {
//            insertedCard = cardRepository.insertRandom(1, insertedDeck.id!!).first()
//        }
//
//        @Test
//        fun `should patch card`() {
//            // given
//            val patchCardRequest =
//                PatchCardRequest(
//                    key = getRandomString("updated"),
//                    value = getRandomString("updated"),
//                )
//
//            // when
//            val actualCard =
//                sendPatchCardAndValidateStatusAndContentType(
//                    insertedDeck.id.toString(),
//                    insertedCard.id.toString(),
//                    patchCardRequest,
//                )
//
//            // then
//            actualCard.key shouldBe patchCardRequest.key
//            actualCard.value shouldBe patchCardRequest.value
//
//            val cardFromMongo = cardRepository.findById(insertedCard.id!!).get()!!
//
//            cardFromMongo.key shouldBe patchCardRequest.key
//
//            cardFromMongo.value shouldBe patchCardRequest.value
//        }
//
//        @Test
//        fun `should return 400 if deck does not exist`() {
//            // given
//            val randomDeckId = getRandomID()
//
//            // when
//            val performPatch =
//                sendPatchCard(
//                    randomDeckId.toString(),
//                    cardId = insertedCard.id.toString(),
//                    PatchCardRequest(),
//                )
//
//            // then
//            val userId = securityService.jwtUtils.getUserIdFromJwtToken(token)
//            val result =
//                performPatch
//                    .andExpect {
//                        status { isBadRequest() }
//                    }
//                    .andReturn()
//
//            result.response.contentAsString shouldBe
//                DeckDoesNotExistException.fromDeckIdAndUserId(randomDeckId.toString(), userId).message
//        }
//
//        @Test
//        fun `should return 400 if card does not exist`() {
//            // given
//            val randomCardId = getRandomID()
//
//            // when
//            val performPatch =
//                mockMvc.patch(patchBaseUrl, insertedDeck.id.toString(), randomCardId.toString()) {
//                    contentType = MediaType.APPLICATION_JSON
//                    content = objectMapper.writeValueAsString(PatchCardRequest())
//                }
//
//            // then
//            performPatch
//                .andExpect {
//                    status { isUnauthorized() }
//                }
//                .andReturn()
//        }
//
//        @Test
//        fun `should return 400 if no auth header exist`() {
//            // given
//            val randomCardId = getRandomID()
//
//            // when
//            val performPatch =
//                sendPatchCard(
//                    insertedDeck.id.toString(),
//                    cardId = randomCardId.toString(),
//                    PatchCardRequest(),
//                )
//
//            // then
//            performPatch
//                .andExpect {
//                    status { isBadRequest() }
//                }
//                .andReturn()
//        }
//
//        private fun sendPatchCardAndValidateStatusAndContentType(
//            deckId: String,
//            cardId: String,
//            patchCardRequest: PatchCardRequest,
//        ): CardDtoResponse =
//            sendPatchCard(deckId, cardId, patchCardRequest)
//                .andExpect {
//                    status { isOk() }
//                    content { contentType(MediaType.APPLICATION_JSON) }
//                }
//                .andReturn()
//                .let { objectMapper.readValue(it.response.contentAsString) }
//
//        private fun sendPatchCard(
//            deckId: String,
//            cardId: String,
//            patchCardRequest: PatchCardRequest,
//        ): ResultActionsDsl =
//            mockMvc.patch(patchBaseUrl, deckId, cardId) {
//                contentType = MediaType.APPLICATION_JSON
//                content = objectMapper.writeValueAsString(patchCardRequest)
//                header(AUTH_HEADER_NAME, TOKEN_PREFIX + token)
//            }
//    }
//
//    @Nested
//    @DisplayName("DELETE ${BASE_URL}${CONCRETE_CARD}")
//    @TestInstance(Lifecycle.PER_CLASS)
//    inner class DeleteCard {
//        private val deleteBaseUrl = BASE_URL + CONCRETE_CARD
//
//        @Test
//        fun `should delete the card`() {
//            // given
//            val model = cardRepository.insertRandom(1, deckId = insertedDeck.id!!).first()
//
//            // when
//            val performDelete = sendDeleteCard(insertedDeck.id!!.toString(), model.id!!.toString())
//            // then
//            val result =
//                performDelete
//                    .andExpect {
//                        status { isNoContent() }
//                    }
//                    .andReturn()
//
//            result.response.contentType shouldBe null
//            result.response.contentAsString.isEmpty() shouldBe true
//
//            cardRepository.existsByIdWithStatus(model.id!!, DocumentStatus.ACTIVE).get() shouldBe false
//            cardRepository.existsByIdWithStatus(model.id!!, DocumentStatus.DELETED).get() shouldBe true
//        }
//
//        @Test
//        fun `should get 204 when no card exists`() {
//            // given
//            val notExistingCardID = ObjectId.get()
//
//            // when
//            val performDelete = sendDeleteCard(insertedDeck.id!!.toString(), notExistingCardID.toString())
//
//            // when/then
//            val result =
//                performDelete
//                    .andDo { print() }
//                    .andExpect { status { isNoContent() } }
//                    .andReturn()
//
//            result.response.contentType shouldBe null
//            result.response.contentAsString.isEmpty() shouldBe true
//
//            cardRepository.existsByIdWithStatus(notExistingCardID, DocumentStatus.ACTIVE).get() shouldBe false
//        }
//
//        @Test
//        fun `should get 400 when deck does not exists`() {
//            // given
//            val randomDeckId = getRandomID()
//
//            // when
//            val performDelete = sendDeleteCard(randomDeckId.toString(), getRandomID().toString())
//
//            // when/then
//            val result =
//                performDelete
//                    .andDo { print() }
//                    .andExpect { status { isBadRequest() } }
//                    .andReturn()
//            val userId = securityService.jwtUtils.getUserIdFromJwtToken(token)
//            result.response.contentAsString shouldBe
//                DeckDoesNotExistException.fromDeckIdAndUserId(randomDeckId.toString(), userId).message
//        }
//
//        private fun sendDeleteCard(deckId: String, cardId: String): ResultActionsDsl =
//            mockMvc.delete(deleteBaseUrl, deckId, cardId) {
//                header(AUTH_HEADER_NAME, TOKEN_PREFIX + token)
//            }
//    }

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
