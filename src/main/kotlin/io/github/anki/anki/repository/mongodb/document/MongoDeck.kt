package io.github.anki.anki.repository.mongodb.document

import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.Instant

@Document(collection = MongoDeck.COLLECTION_NAME)
data class MongoDeck(
    @Id
    @Field(MongoDocument.ID)
    override var id: ObjectId? = null,
    @CreatedDate
    @Field(MongoDocument.CREATED_AT)
    override val createdAt: Instant? = null,
    @LastModifiedDate
    @Field(MongoDocument.MODIFIED_AT)
    override val modifiedAt: Instant? = null,
    @Field(MongoDocument.DOCUMENT_STATUS)
    override val status: DocumentStatus = DocumentStatus.ACTIVE,
    @Indexed
    @Field(USER_ID)
    var userId: ObjectId,
    @Field(NAME)
    val name: String? = null,
    @Field(DESCRIPTION)
    val description: String? = null,
) : MongoDocument {

    companion object {
        const val COLLECTION_NAME = "decks"
        const val USER_ID = "userId"
        const val NAME = "name"
        const val DESCRIPTION = "description"
    }
}
