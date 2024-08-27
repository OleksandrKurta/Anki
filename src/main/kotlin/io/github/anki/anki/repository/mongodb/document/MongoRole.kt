package io.github.anki.anki.repository.mongodb.document

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document(collection = MongoRole.COLLECTION_NAME)
data class MongoRole(
    @Id
    @Field(MongoDocument.ID)
    val id: ObjectId? = null,
    @Field(ROLE_NAME)
    var name: String? = null,
) {
    companion object {
        const val COLLECTION_NAME = "role"

        const val ROLE_NAME = "name"
    }
}
