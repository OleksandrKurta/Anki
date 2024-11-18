package io.github.anki.anki.repository.mongodb.document

import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = MongoDeckEvent.COLLECTION_NAME)
data class MongoDeckEvent(
    @Id
    override val id: ObjectId? = null,
    @CreatedDate
    override val createdAt: Instant? = null,
    @LastModifiedDate
    override val modifiedAt: Instant? = null,
    override val status: DocumentStatus = DocumentStatus.ACTIVE,
    @Indexed
    val deckId: ObjectId? = null,
    val event: String? = null,
) : MongoDocument {
    companion object {
        const val COLLECTION_NAME = "deck_logs"
        const val DECK_ID = "deckId"
        const val EVENT = "event"
    }
}
