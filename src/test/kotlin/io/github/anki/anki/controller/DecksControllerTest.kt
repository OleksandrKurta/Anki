package io.github.anki.anki.controller

import com.fasterxml.jackson.databind.ObjectMapper
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
import io.github.anki.anki.service.model.User
import io.github.anki.anki.service.model.mapper.toDeck
import io.github.anki.anki.service.model.mapper.toMongo
import io.github.anki.anki.service.model.mapper.toMongoUser
import io.github.anki.anki.service.model.mapper.toUser
import io.github.anki.anki.service.secure.AuthenticationManager
import io.github.anki.anki.service.secure.jwt.JwtUtils.Companion.AUTH_HEADER_NAME
import io.github.anki.anki.service.secure.jwt.JwtUtils.Companion.TOKEN_PREFIX
import io.github.anki.anki.service.utils.toObjectId
import io.github.anki.testing.IntegrationTestWithClient
import io.github.anki.testing.getDtoFromResponseBody
import io.github.anki.testing.getRandomID
import io.github.anki.testing.getRandomString
import io.github.anki.testing.insertRandom
import io.github.anki.testing.randomUser
import io.github.anki.testing.testcontainers.TestContainersFactory
import io.github.anki.testing.testcontainers.with
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
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import java.nio.charset.StandardCharsets
import java.util.stream.Stream
import kotlin.test.BeforeTest
import kotlin.test.Test

@IntegrationTestWithClient
class DecksControllerTest @Autowired constructor(
    private val objectMapper: ObjectMapper,
    private val deckRepository: DeckRepository,
    private val cardRepository: CardRepository,
    private val userRepository: UserRepository,
    private val authenticationManager: AuthenticationManager,
    private val encoder: PasswordEncoder,
    private val webTestClient: WebTestClient,
) {

    private lateinit var user: User
    private lateinit var token: String

    @BeforeTest
    fun setUp() {
        val newUser = SignUpRequestDto.randomUser().toUser(encoder)
        val mongoUser = userRepository.insert(newUser.toMongoUser()).block()!!
        user = mongoUser.toUser()
        token = authenticationManager
            .authenticate(newUser)
            .map { it.creds }
            .block()!!
    }

    @Nested
    @DisplayName("POST ${BASE_URL}")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class PostDeck {

        private lateinit var newDeckRequest: NewDeckRequest

        @BeforeTest
        fun createNewDeckRequest() {
            newDeckRequest =
                NewDeckRequest(
                    name = getRandomString("initial"),
                    description = getRandomString("initial"),
                )
        }

        @Test
        fun `should create new Deck`() {
            // when
            val response = postNewDeck(newDeckRequest)

            // then
            response
                .expectStatus()
                .isCreated
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)

            val createdDeck: DeckDtoResponse = response.getDtoFromResponseBody(objectMapper)

            createdDeck.name shouldBe newDeckRequest.name
            createdDeck.description shouldBe newDeckRequest.description

            StepVerifier
                .create(
                    deckRepository.findById(ObjectId(createdDeck.id)),
                )
                .assertNext {
                    it.toDeck().toDto() shouldBe createdDeck
                }
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
            val response = postNewDeck(newDeckRequest)

            // then
            response
                .expectStatus()
                .isBadRequest
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody(String::class.java)
                .isEqualTo("{\"name\":\"must not be blank\"}")
        }

        private fun postNewDeck(newDeck: NewDeckRequest): WebTestClient.ResponseSpec =
            webTestClient
                .post()
                .uri(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(newDeck))
                .header(AUTH_HEADER_NAME, TOKEN_PREFIX + token)
                .exchange()

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
            val numberOfRandomCards = (5..100).random()
            val insertedDecks = deckRepository
                .insertRandom(numberOfRandomCards, user.id!!.toObjectId())
                .collectList()
                .block()!!

            // when/then
            sendGetDecks()
                .expectStatus()
                .isOk
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(DeckDtoResponse::class.java)
                .isEqualTo<WebTestClient.ListBodySpec<DeckDtoResponse>>(insertedDecks.map { it.toDeck().toDto() })
        }

        private fun sendGetDecks(): WebTestClient.ResponseSpec =
            webTestClient
                .get()
                .uri(BASE_URL)
                .header(AUTH_HEADER_NAME, TOKEN_PREFIX + token)
                .exchange()
    }

    @Nested
    @DisplayName("PATCH ${BASE_URL}${CONCRETE_DECK}")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class PatchDeck {

        private val patchBaseUrl = BASE_URL + CONCRETE_DECK

        @Test
        fun `should patch deck if it exists`() {
            // given
            val insertedDeck = deckRepository
                .insertRandom(1, user.id!!.toObjectId())
                .blockFirst()!!

            val patchDeckRequest =
                PatchDeckRequest(
                    name = getRandomString("updated"),
                    description = getRandomString("updated"),
                )

            // when/then
            sendPatchDeck(insertedDeck.id.toString(), patchDeckRequest)
                .expectStatus()
                .isOk
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody(DeckDtoResponse::class.java)
                .isEqualTo(patchDeckRequest.toDeck(insertedDeck.id.toString(), user.id!!).toDto())

            StepVerifier
                .create(
                    deckRepository.findById(insertedDeck.id!!),
                )
                .assertNext {
                    it.name shouldBe patchDeckRequest.name
                    it.description shouldBe patchDeckRequest.description
                }
                .verifyComplete()
        }

        @Test
        fun `should return 400 if it does not exist`() {
            // given
            val notExistingDeckID = getRandomID().toString()

            // when/then
            sendPatchDeck(notExistingDeckID, PatchDeckRequest())
                .expectStatus()
                .isBadRequest
                .expectHeader()
                .contentType(MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                .expectBody(String::class.java)
                .isEqualTo(DeckDoesNotExistException.fromDeckIdAndUserId(notExistingDeckID, user.id!!).message)

            StepVerifier
                .create(
                    deckRepository.existsById(ObjectId(notExistingDeckID)),
                )
                .expectNext(false)
                .verifyComplete()
        }

        private fun sendPatchDeck(deckId: String, patchDeckRequest: PatchDeckRequest): WebTestClient.ResponseSpec =
            webTestClient
                .patch()
                .uri { it.path(patchBaseUrl).build(deckId) }
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(patchDeckRequest))
                .header(AUTH_HEADER_NAME, TOKEN_PREFIX + token)
                .exchange()
    }

    @Nested
    @DisplayName("DELETE ${BASE_URL}${CONCRETE_DECK}")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class DeleteDeck {

        private val deleteBaseUrl = BASE_URL + CONCRETE_DECK

        @Test
        fun `should delete the deck`() {
            // given
            val newDeckRequest =
                NewDeckRequest(
                    name = getRandomString("initial"),
                    description = getRandomString("initial"),
                )
            val insertedDeck =
                deckRepository
                    .insert(newDeckRequest.toDeck(user.id!!).toMongo())
                    .block()!!
            val insertedCards =
                cardRepository
                    .insertRandom((5..100).random(), insertedDeck.id!!)
                    .collectList()
                    .block()!!

            // when/then
            sendDeleteDeck(insertedDeck.id!!.toString())
                .expectStatus()
                .isNoContent
                .expectBody()
                .isEmpty

            StepVerifier
                .create(
                    Flux
                        .zip(
                            deckRepository.existsByIdWithStatus(insertedDeck.id!!, DocumentStatus.ACTIVE),
                            deckRepository.existsByIdWithStatus(insertedDeck.id!!, DocumentStatus.DELETED),
                            cardRepository.findByDeckIdWithStatus(insertedDeck.id!!).collectList(),
                            cardRepository
                                .findByDeckIdWithStatus(
                                    insertedDeck.id!!,
                                    DocumentStatus.DELETED,
                                    limit = insertedCards.size,
                                )
                                .collectList(),
                        ),
                )
                .assertNext {
                    it.t1 shouldBe false
                    it.t2 shouldBe true
                    it.t3.isEmpty() shouldBe true
                    it.t4.size shouldBe insertedCards.size
                }
                .verifyComplete()
        }

        private fun sendDeleteDeck(deckId: String): WebTestClient.ResponseSpec =
            webTestClient
                .delete()
                .uri { it.path(deleteBaseUrl).build(deckId) }
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
