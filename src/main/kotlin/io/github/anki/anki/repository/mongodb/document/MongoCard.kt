package io.github.anki.anki.repository.mongodb.document

import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = MongoCard.COLLECTION_NAME)
data class MongoCard(
    @Id
    val id: ObjectId? = null,
    @Indexed
    val deckId: ObjectId? = null,
    var cardKey: String? = null,
    var cardValue: String? = null,
    @CreatedDate
    val createdAt: Instant? = null,
    @LastModifiedDate
    val modifiedAt: Instant? = null,
    val status: Status = Status.ACTIVE,
) {

    enum class Status {
        ACTIVE,
        DELETED,
    }

    companion object {
        const val COLLECTION_NAME = "cards"

        const val ID = "id"
        const val DECK_ID = "deckId"
        const val CARD_KEY = "cardKey"
        const val CARD_VALUE = "cardValue"
        const val CREATED_AT = "createdAt"
        const val MODIFIED_AT = "modifiedAt"
        const val STATUS = "status"
    }
}
