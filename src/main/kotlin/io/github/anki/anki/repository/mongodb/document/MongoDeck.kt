package io.github.anki.anki.repository.mongodb.document

import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = MongoDeck.COLLECTION_NAME)
data class MongoDeck(
    @Id
    val id: ObjectId? = null,
    @Indexed
    val userId: ObjectId,
    val name: String? = null,
    val description: String? = null,
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
        const val COLLECTION_NAME = "decks"

        const val ID = "id"
        const val USER_ID = "userId"
        const val NAME = "name"
        const val DESCRIPTION = "description"
        const val CREATED_AT = "createdAt"
        const val MODIFIED_AT = "modifiedAt"
        const val STATUS = "status"
    }
}
