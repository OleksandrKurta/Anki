package io.github.anki.anki.repository.mongodb.document

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document(collection = MongoRole.COLLECTION_NAME)
data class MongoRole(
    @Id
    @Field(MongoDocument.ID)
    var id: ObjectId? = null,
    @Field(ROLE_NAME)
    var name: Role? = null,
) {
    companion object {
        const val COLLECTION_NAME = "role"

        const val ROLE_NAME = "name"
    }
}
