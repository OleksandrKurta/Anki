//package io.github.anki.anki.repository
//import io.github.anki.anki.repository.mongodb.CardRepository
//import io.github.anki.anki.repository.mongodb.document.MongoCard
//import org.junit.jupiter.api.DisplayName
//import org.junit.jupiter.api.Test
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.util.Assert
//import java.util.*
//
//
//@SpringBootTest()
//class CardRepositoryTest @Autowired constructor(
//    val dataSource: CardRepository,
//){
//
//   @DisplayName("Insert card")
//    @Test
//    fun `should insert card`() {
//        val cardModel = MongoCard(
//            parentCollectionId = UUID.randomUUID().toString(),
//            cardKey = UUID.randomUUID().toString(),
//            cardValue = UUID.randomUUID().toString())
//        dataSource.insert(cardModel)
//        Assert.isTrue(dataSource.existsById(cardModel.id.toString()), "Card appear in repository")
//    }
//}
