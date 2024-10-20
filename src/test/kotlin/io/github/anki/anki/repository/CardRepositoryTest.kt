//package io.github.anki.anki.repository
//
//import io.github.anki.anki.repository.mongodb.CardRepository
//import io.github.anki.anki.repository.mongodb.document.DocumentStatus
//import io.github.anki.anki.repository.mongodb.document.MongoCard
//import io.github.anki.testing.IntegrationTest
//import io.github.anki.testing.getRandomID
//import io.github.anki.testing.getRandomString
//import io.github.anki.testing.testcontainers.TestContainersFactory
//import io.github.anki.testing.testcontainers.with
//import io.kotest.matchers.shouldBe
//import io.kotest.matchers.shouldNotBe
//import org.junit.jupiter.api.Test
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.test.context.DynamicPropertyRegistry
//import org.springframework.test.context.DynamicPropertySource
//import org.testcontainers.containers.MongoDBContainer
//import org.testcontainers.junit.jupiter.Container
//import kotlin.test.BeforeTest
//
//@IntegrationTest
//class CardRepositoryTest @Autowired constructor(
//    val cardRepository: CardRepository,
//) {
//    private lateinit var newCard: MongoCard
//
//    @BeforeTest
//    fun setUp() {
//        newCard =
//            MongoCard(
//                deckId = getRandomID(),
//                key = getRandomString(),
//                value = getRandomString(),
//            )
//    }
//
//    @Test
//    fun `should insert card`() {
//        // when
//        val cardFromMongo = cardRepository.insert(newCard).get()
//
//        // then
//        cardFromMongo.id shouldNotBe null
//        cardRepository.existsByIdWithStatus(cardFromMongo.id!!, DocumentStatus.ACTIVE).get() shouldBe true
//    }
//
//    @Test
//    fun `should soft delete existing card by id`() {
//        // given
//        val cardFromMongo = cardRepository.insert(newCard).get()
//
//        // when
//        cardRepository.softDelete(cardFromMongo.id!!).get()
//
//        // then
//        cardRepository.existsByIdWithStatus(cardFromMongo.id!!, DocumentStatus.ACTIVE).get() shouldBe false
//        cardRepository.existsByIdWithStatus(cardFromMongo.id!!, DocumentStatus.DELETED).get() shouldBe true
//    }
//
//    @Test
//    fun `should delete NOT existing card by id`() {
//        // given
//        val notExistingCardId = getRandomID()
//
//        // when
//        cardRepository.softDelete(notExistingCardId).get()
//
//        // then
//        cardRepository.existsById(notExistingCardId).get() shouldBe false
//    }
//
//    @Test
//    fun `should NOT find card by not exist deckId`() {
//        // given
//        val notExistingDeckId = getRandomID()
//
//        // when
//        val card = cardRepository.findByDeckIdWithStatus(deckId = notExistingDeckId).get()
//
//        // then
//        card shouldBe listOf()
//    }
//
//    @Test
//    fun `should find card by deckId`() {
//        // given
//        val expectedCard = cardRepository.insert(newCard).get()
//
//        // when
//        val card = cardRepository.findByDeckIdWithStatus(deckId = newCard.deckId!!).get()
//
//        // then
//        card shouldBe listOf(expectedCard)
//    }
//
//    @Test
//    fun `should soft delete by card by deckId`() {
//        // given
//        val expectedCard = cardRepository.insert(newCard).get()
//
//        // when
//        cardRepository.softDeleteByDeckId(deckId = newCard.deckId!!).get()
//
//        // then
//        cardRepository.findByIdWithStatus(expectedCard.id!!, DocumentStatus.ACTIVE).get() shouldBe null
//    }
//
//    companion object {
//        @Container
//        @Suppress("PropertyName")
//        private val mongoDBContainer: MongoDBContainer = TestContainersFactory.newMongoContainer()
//
//        @DynamicPropertySource
//        @JvmStatic
//        fun setProperties(registry: DynamicPropertyRegistry) {
//            registry.with(mongoDBContainer)
//        }
//    }
//}
