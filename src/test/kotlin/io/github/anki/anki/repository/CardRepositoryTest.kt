package io.github.anki.anki.repository
import io.github.anki.anki.repository.mongodb.CardRepository
import io.github.anki.anki.repository.mongodb.document.MongoCard
import io.kotest.matchers.shouldBe
import org.bson.types.ObjectId
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.util.Assert
import java.util.*


@SpringBootTest()
class CardRepositoryTest @Autowired constructor(
    val cardRepository: CardRepository,
){

   @DisplayName("Insert card")
    @Test
    fun `should insert card`() {
        val cardModel = MongoCard(
            deckId = ObjectId(),
            cardKey = UUID.randomUUID().toString(),
            cardValue = UUID.randomUUID().toString())
       cardRepository.insert(cardModel)
        Assert.isTrue(cardRepository.existsById(cardModel.id), "Card appear in repository")
    }
}
