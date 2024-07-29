package io.github.anki.anki.repository
import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.model.MongoCard
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.util.Assert
import java.util.*
import kotlin.test.AfterTest
import kotlin.test.BeforeTest


@IntegrationTest
class CardRepositoryTest @Autowired constructor(
    val cardRepository: CardRepository,
){

    private lateinit var cleanupModels: MutableList<MongoCard>
    private lateinit var newCard: MongoCard

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

    fun generateRandomCard(): MongoCard =
        MongoCard(
            deckId = ObjectId(),
            cardKey = UUID.randomUUID().toString(),
            cardValue =UUID.randomUUID().toString(),
        )

    @Test
    fun `should insert card`() {
        // given
        val cardFromMongo = cardRepository.insert(newCard)
        cleanupModels.add(cardFromMongo)

        // when, then
        cardFromMongo.id shouldNotBe null
        Assert.isTrue(cardRepository.existsById(cardFromMongo.id!!), "Card appear in repository")
    }

    @Test
    fun `should delete existing card by id`() {
        //given
        val cardFromMongo = cardRepository.insert(newCard)
        cleanupModels.add(cardFromMongo)

        //when
        cardRepository.deleteById(cardFromMongo.id.toString())

        //then
        cardRepository.existsById(cardFromMongo.id!!) shouldBe false

    }

    companion object {
        private val LOG = LoggerFactory.getLogger(CardsService::class.java)

        @Container
        private val mongoDBContainer: MongoDBContainer = MongoDBContainer("mongo:7")

        @DynamicPropertySource
        @JvmStatic
        fun setProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.mongodb.uri") { mongoDBContainer.replicaSetUrl }
        }

    }
}
