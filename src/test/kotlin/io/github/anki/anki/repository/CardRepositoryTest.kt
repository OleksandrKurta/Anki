package io.github.anki.anki.repository

import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.document.MongoCard
import io.github.anki.testing.IntegrationTest
import io.github.anki.testing.getRandomID
import io.github.anki.testing.getRandomString
import io.github.anki.testing.testcontainers.TestContainersFactory
import io.github.anki.testing.testcontainers.with
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.util.Assert
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import kotlin.test.BeforeTest


@IntegrationTest
class CardRepositoryTest @Autowired constructor(
    val cardRepository: CardRepository,
){

    private lateinit var newCard: MongoCard

    @BeforeTest
    fun setUp() {
        LOG.info("Initializing new MongoCard")
        newCard = generateRandomCard()
    }

    @Test
    fun `should insert card`() {
        // given
        val cardFromMongo = cardRepository.insert(newCard)

        // when, then
        cardFromMongo.id shouldNotBe null
        Assert.isTrue(cardRepository.existsById(cardFromMongo.id!!), "Card appear in repository")
    }

    @Test
    fun `should delete existing card by id`() {
        //given
        val cardFromMongo = cardRepository.insert(newCard)

        //when
        cardRepository.deleteById(cardFromMongo.id.toString())

        //then
        cardRepository.existsById(cardFromMongo.id!!) shouldBe false

    }

    @Test
    fun `should delete NOT existing card by id`() {
        // given
        val notExistingCardId = getRandomID()

        //when
        cardRepository.deleteById(notExistingCardId)

        //then
        cardRepository.existsById(notExistingCardId) shouldBe false

    }

    private fun generateRandomCard(): MongoCard =
        MongoCard(
            deckId = getRandomID(),
            cardKey = getRandomString(),
            cardValue = getRandomString(),
        )

    companion object {
        private val LOG = LoggerFactory.getLogger(CardRepositoryTest::class.java)

        @Container
        private val mongoDBContainer: MongoDBContainer = TestContainersFactory.newMongoContainer()

        @DynamicPropertySource
        @JvmStatic
        fun setProperties(registry: DynamicPropertyRegistry) {
            registry.with(mongoDBContainer)
        }

    }
}
