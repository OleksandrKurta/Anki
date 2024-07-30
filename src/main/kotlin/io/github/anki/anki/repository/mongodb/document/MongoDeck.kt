package io.github.anki.anki.repository.mongodb.document
import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant


@Document(collation = MongoDeck.COLLECTION_NAME)
data class MongoDeck(
    @Id
    var id: ObjectId? = ObjectId(),

    @Indexed
    var userId: ObjectId,

    var name: String? = null,

    var description: String? = null,

    @CreatedDate
    val createdAt: Instant? = null,

    @LastModifiedDate
    val modifiedAt: Instant? = null,
) {
    companion object {
        const val COLLECTION_NAME = "decks"
    }
}
