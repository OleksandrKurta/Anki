package io.github.anki.anki.repository.mongodb.document

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size
import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.Instant

@Document(collection = MongoUser.COLLECTION_NAME)
data class MongoUser(
    @Id
    @Field(MongoDocument.ID)
    override var id: ObjectId? = null,
    @Field(USER_NAME)
    @Size(max = 20)
    @Indexed(unique = true)
    val userName: String? = null,
    @Size(max = 50)
    @Email
    @Field(EMAIL)
    @Indexed(unique = true)
    val email: String? = null,
    @Size(max = 120)
    @Field(PASSWORD)
    val password: String? = null,
    @Field(ROLES)
    val roles: Set<String?> = HashSet(),
    @Field(MongoDocument.CREATED_AT)
    @CreatedDate
    override val createdAt: Instant? = null,
    @Field(MongoDocument.MODIFIED_AT)
    @LastModifiedDate
    override val modifiedAt: Instant? = null,
    @Field(MongoDocument.DOCUMENT_STATUS)
    override val status: DocumentStatus = DocumentStatus.ACTIVE,
) : MongoDocument {

    companion object {
        const val COLLECTION_NAME = "user"

        const val USER_NAME = "userName"
        const val EMAIL = "email"
        const val PASSWORD = "password"
        const val ROLES = "roles"
    }
}
