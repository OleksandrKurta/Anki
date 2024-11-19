package io.github.anki.anki.repository.mongodb.document

import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.Instant
import java.util.*

@Document(collection = MongoRateLog.COLLECTION_NAME)
class MongoRateLog(
    @Id
    @Field(MongoDocument.ID)
    override var id: ObjectId? = null,
    @Field(MongoDocument.CREATED_AT)
    @CreatedDate
    override var createdAt: Instant? = null,
    @Field(MongoDocument.MODIFIED_AT)
    @LastModifiedDate
    override var modifiedAt: Instant? = null,
    @Field(MongoDocument.DOCUMENT_STATUS)
    override var status: DocumentStatus = DocumentStatus.ACTIVE,
    @Field(LEARNING_RATE)
    var learningRate: String,
    @Field(LEARNING_ITEM_ID)
    var learningItemId: ObjectId,
    @Field(USER_ID)
    var userId: ObjectId,
    @Field(DECK_ID)
    var deckId: ObjectId,
) : MongoDocument {

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as MongoRateLog
        return id == that.id && status == that.status && learningItemId == that.learningItemId
    }

    override fun hashCode(): Int {
        return Objects.hash(id, status, learningItemId)
    }

    companion object {
        const val COLLECTION_NAME: String = "rate"
        const val LEARNING_RATE: String = "learningRate"
        const val LEARNING_ITEM_ID: String = "learningItemId"
        const val USER_ID: String = "userId"
        const val DECK_ID: String = "deckId"
    }
}
