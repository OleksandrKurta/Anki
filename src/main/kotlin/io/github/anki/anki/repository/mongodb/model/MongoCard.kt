package io.github.anki.anki.repository.mongodb.model
import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant


@Document(collection = MongoCard.COLLECTION_NAME)
data class MongoCard(
    @Id
    var id: ObjectId? = ObjectId(),
    val cardKey: String? = null,
    val cardValue: String? = null,

    val parentCollectionId: String? = null,

    @CreatedDate
    val createdAt: Instant? = null,
    @LastModifiedDate
    val modifiedAt: Instant? = null,
) {
  companion object {
        const val COLLECTION_NAME = "card"
    }
}

