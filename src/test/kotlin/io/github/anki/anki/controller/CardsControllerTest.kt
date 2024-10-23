package io.github.anki.anki.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.anki.anki.controller.CardsController.Companion.BASE_URL
import io.github.anki.anki.controller.CardsController.Companion.CONCRETE_CARD
import io.github.anki.anki.controller.dto.CardDtoResponse
import io.github.anki.anki.controller.dto.NewCardRequest
import io.github.anki.anki.controller.dto.PaginationDto
import io.github.anki.anki.controller.dto.PatchCardRequest
import io.github.anki.anki.controller.dto.auth.SignUpRequestDto
import io.github.anki.anki.controller.dto.mapper.toCard
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
import io.github.anki.anki.service.model.mapper.toUser
import io.github.anki.anki.service.secure.AuthenticationManager
import io.github.anki.anki.service.secure.jwt.JwtUtils.Companion.AUTH_HEADER_NAME
import io.github.anki.anki.service.secure.jwt.JwtUtils.Companion.TOKEN_PREFIX
import io.github.anki.anki.service.utils.toObjectId
import io.github.anki.testing.DATA_PREFIX
import io.github.anki.testing.ReactiveIntegrationTest
import io.github.anki.testing.getDtoFromResponseBody
import io.github.anki.testing.getListOfDtoFromResponseBody
import io.github.anki.testing.getRandomID
import io.github.anki.testing.getRandomString
import io.github.anki.testing.insertRandom
import io.github.anki.testing.randomUser
import io.github.anki.testing.testcontainers.TestContainersFactory
import io.github.anki.testing.testcontainers.with
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import reactor.test.StepVerifier
import java.nio.charset.StandardCharsets
import kotlin.test.BeforeTest

@ReactiveIntegrationTest
class CardsControllerTest @Autowired constructor(
    private val objectMapper: ObjectMapper,
    private val cardRepository: CardRepository,
    private val deckRepository: DeckRepository,
    private val userRepository: UserRepository,
    private val authenticationManager: AuthenticationManager,
    private val encoder: PasswordEncoder,
    private val webTestClient: WebTestClient,
) {

    private lateinit var user: User
    private lateinit var insertedDeck: MongoDeck
    private lateinit var token: String

    @BeforeTest
    fun setUp() {
        val newUser = SignUpRequestDto.randomUser()
        val mongoUser = userRepository.insert(newUser.toUser(encoder.encode(newUser.password)).toMongoUser()).block()!!
        user = mongoUser.toUser()
        token = authenticationManager
            .authenticate(newUser.toUser())
            .map { it.creds }
            .block()!!
        insertedDeck = deckRepository.insertRandom(1, userId = mongoUser.id!!).blockFirst()!!
    }

    @Nested
    @DisplayName("POST ${BASE_URL}")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class PostCard {

        private lateinit var newCard: NewCardRequest

        @BeforeTest
        fun createNewCardRequest() {
            newCard =
                NewCardRequest(
                    key = getRandomString(DATA_PREFIX),
                    value = getRandomString(DATA_PREFIX),
                )
        }

        @Test
        fun `should create new Card always`() {
            // when
            val response: WebTestClient.ResponseSpec = postNewCard(newCard, insertedDeck.id.toString())

            // then
            response
                .expectStatus()
                .isCreated
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)

            val createdCard = response.getDtoFromResponseBody<CardDtoResponse>(objectMapper)

            createdCard.key shouldBe newCard.key
            createdCard.value shouldBe newCard.value

            StepVerifier
                .create(
                    cardRepository
                        .findById(createdCard.id.toObjectId())
                        .map { it.toCard().toDto() },
                )
                .expectNext(createdCard)
                .verifyComplete()
        }

        @Test
        fun `should return 400 if deck does not exist`() {
            // given
            val randomDeckId = getRandomID()

            // when/then
            postNewCard(newCard, randomDeckId.toString())
                .expectStatus()
                .isBadRequest
                .expectHeader()
                .contentType(MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                .expectBody(String::class.java)
                .isEqualTo(DeckDoesNotExistException.fromDeckIdAndUserId(randomDeckId.toString(), user.id!!).message)
            StepVerifier
                .create(cardRepository.findByDeckIdWithStatus(randomDeckId, DocumentStatus.ACTIVE))
                .expectNextCount(0)
                .verifyComplete()
        }

        @Test
        fun `should return authException token was not in header`() {
            // when/then
            webTestClient
                .post()
                .uri { uriBuilder -> uriBuilder.path(BASE_URL).build(getRandomID()) }
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(newCard))
                .exchange()
                .expectStatus()
                .isUnauthorized
        }

        private fun postNewCard(newCard: NewCardRequest, deckId: String): WebTestClient.ResponseSpec =
            webTestClient
                .post()
                .uri { uriBuilder -> uriBuilder.path(BASE_URL).build(deckId) }
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(newCard))
                .header(AUTH_HEADER_NAME, TOKEN_PREFIX + token)
                .exchange()
    }

    @Nested
    @DisplayName("GET ${BASE_URL}")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class GetCards {
        @Test
        fun `should return all cards if they exist`() {
            // given
            val mongoCards = cardRepository
                .insertRandom((5..100).random(), insertedDeck.id!!)
                .collectList()
                .block()!!

            // when
            val response = sendGetCards(insertedDeck.id!!.toString(), PaginationDto(limit = 100))

            // then
            response
                .expectStatus()
                .isOk
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(CardDtoResponse::class.java)
                .isEqualTo<WebTestClient.ListBodySpec<CardDtoResponse>>(mongoCards.map { it.toCard().toDto() })
        }

        @ParameterizedTest
        @ValueSource(ints = [50, 75, 100, 101, 231, 1234])
        fun `should return cards with pagination`(cardsAmount: Int) {
            // given
            val mongoCards = cardRepository.insertRandom(cardsAmount, insertedDeck.id!!).collectList().block()!!

            val cardsFromResponses = mutableListOf<CardDtoResponse>()

            var requestCounter = 0

            // when
            do {
                val paginationDto = PaginationDto(offset = cardsFromResponses.size)
                val response =
                    sendGetCards(deckId = insertedDeck.id!!.toString(), paginationDto = paginationDto)
                        .expectStatus()
                        .isOk
                        .expectHeader()
                        .contentType(MediaType.APPLICATION_JSON)
                val cardsFromThisResponse: List<CardDtoResponse> = response.getListOfDtoFromResponseBody(objectMapper)

                cardsFromResponses.addAll(cardsFromThisResponse)
                requestCounter++
            } while (cardsFromThisResponse.size == paginationDto.limit)

            // then
            cardsFromResponses shouldHaveSize mongoCards.size
            cardsFromResponses shouldContainExactlyInAnyOrder mongoCards.map { it.toCard().toDto() }

            requestCounter shouldBe cardsAmount / PaginationDto.DEFAULT_LIMIT + 1
        }

        private fun sendGetCards(deckId: String, paginationDto: PaginationDto): WebTestClient.ResponseSpec =
            webTestClient
                .get()
                .uri {
                    it
                        .path(BASE_URL)
                        .queryParam(PaginationDto.LIMIT, paginationDto.limit.toString())
                        .queryParam(PaginationDto.OFFSET, paginationDto.offset.toString())
                        .build(deckId)
                }
                .header(AUTH_HEADER_NAME, TOKEN_PREFIX + token)
                .exchange()
    }

    @Nested
    @DisplayName("PATCH ${BASE_URL}")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class PatchCard {
        private val patchBaseUrl = BASE_URL + CONCRETE_CARD
        private lateinit var insertedCard: MongoCard

        @BeforeTest
        fun createCard() {
            insertedCard = cardRepository.insertRandom(1, insertedDeck.id!!).blockFirst()!!
        }

        @Test
        fun `should patch card`() {
            // given
            val patchCardRequest =
                PatchCardRequest(
                    key = getRandomString("updated"),
                    value = getRandomString("updated"),
                )

            // when/then
            sendPatchCard(insertedDeck.id.toString(), insertedCard.id.toString(), patchCardRequest)
                .expectStatus()
                .isOk
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody(CardDtoResponse::class.java)
                .isEqualTo(patchCardRequest.toCard(insertedCard.id.toString(), insertedDeck.id.toString()).toDto())
            StepVerifier
                .create(cardRepository.findById(insertedCard.id!!))
                .assertNext {
                    it.key shouldBe patchCardRequest.key
                    it.value shouldBe patchCardRequest.value
                }
                .verifyComplete()
        }

        @Test
        fun `should return 400 if deck does not exist`() {
            // given
            val randomDeckId = getRandomID()

            // when
            val response = sendPatchCard(randomDeckId.toString(), insertedCard.id.toString(), PatchCardRequest())

            // then
            response
                .expectStatus()
                .isBadRequest
                .expectHeader()
                .contentType(MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                .expectBody(String::class.java)
                .isEqualTo(DeckDoesNotExistException.fromDeckIdAndUserId(randomDeckId.toString(), user.id!!).message)
        }

        @Test
        fun `should return 400 if card does not exist`() {
            sendPatchCard(insertedDeck.id.toString(), getRandomID().toString(), PatchCardRequest())
                .expectStatus()
                .isBadRequest
        }

        private fun sendPatchCard(
            deckId: String,
            cardId: String,
            patchCardRequest: PatchCardRequest,
        ): WebTestClient.ResponseSpec =
            webTestClient
                .patch()
                .uri { it.path(patchBaseUrl).build(deckId, cardId) }
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(patchCardRequest))
                .header(AUTH_HEADER_NAME, TOKEN_PREFIX + token)
                .exchange()
    }

    @Nested
    @DisplayName("DELETE ${BASE_URL}${CONCRETE_CARD}")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class DeleteCard {
        private val deleteBaseUrl = BASE_URL + CONCRETE_CARD

        @Test
        fun `should delete the card`() {
            // given
            val mongoCard = cardRepository.insertRandom(1, deckId = insertedDeck.id!!).blockFirst()!!

            // when/then
            sendDeleteCard(insertedDeck.id!!.toString(), mongoCard.id.toString())
                .expectStatus()
                .isNoContent
                .expectBody()
                .isEmpty
            StepVerifier
                .create(cardRepository.existsByIdWithStatus(mongoCard.id!!, DocumentStatus.ACTIVE))
                .expectNext(false)
                .verifyComplete()
            StepVerifier
                .create(cardRepository.existsByIdWithStatus(mongoCard.id!!, DocumentStatus.DELETED))
                .expectNext(true)
                .verifyComplete()
        }

        @Test
        fun `should get 204 when no card exists`() {
            sendDeleteCard(insertedDeck.id!!.toString(), getRandomID().toString())
                .expectStatus()
                .isNoContent
                .expectBody()
                .isEmpty
        }

        @Test
        fun `should get 400 when deck does not exists`() {
            val randomDeckId = getRandomID().toString()
            sendDeleteCard(randomDeckId, getRandomID().toString())
                .expectStatus()
                .isBadRequest
                .expectHeader()
                .contentType(MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                .expectBody(String::class.java)
                .isEqualTo(DeckDoesNotExistException.fromDeckIdAndUserId(randomDeckId, user.id!!).message)
        }

        private fun sendDeleteCard(deckId: String, cardId: String): WebTestClient.ResponseSpec =
            webTestClient
                .delete()
                .uri { it.path(deleteBaseUrl).build(deckId, cardId) }
                .header(AUTH_HEADER_NAME, TOKEN_PREFIX + token)
                .exchange()
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
