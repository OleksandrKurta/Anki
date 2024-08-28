package io.github.anki.anki.repository.mongodb.document

import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.Instant

@Document(collection = MongoCard.COLLECTION_NAME)
data class MongoCard(
    @Id
    @Field(MongoDocument.ID)
    override val id: ObjectId? = null,
    @CreatedDate
    @Field(MongoDocument.CREATED_AT)
    override val createdAt: Instant? = null,
    @LastModifiedDate
    @Field(MongoDocument.MODIFIED_AT)
    override val modifiedAt: Instant? = null,
    @Field(MongoDocument.DOCUMENT_STATUS)
    override val status: DocumentStatus = DocumentStatus.ACTIVE,
    @Field(DECK_ID)
    @Indexed
    val deckId: ObjectId? = null,
    @Field(KEY)
    val key: String? = null,
    @Field(VALUE)
    val value: String? = null,
) : MongoDocument {

    companion object {
        const val COLLECTION_NAME = "cards"

        const val DECK_ID = "deckId"
        const val KEY = "key"
        const val VALUE = "value"
    }
}
