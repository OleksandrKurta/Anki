package io.github.anki.anki.repository.mongodb.document

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = MongoUser.COLLECTION_NAME)
class MongoUser {
    @Id
    var id: String? = null

    @NotBlank
    @Size(max = 20)
    var username: String? = null

    @NotBlank
    @Size(max = 50)
    @Email
    var email: String? = null

    @NotBlank
    @Size(max = 120)
    var password: String? = null

    @DBRef
    var roles: Set<MongoRole?> = HashSet<MongoRole?>()

    constructor()

    constructor(username: String?, email: String?, password: String?) {
        this.username = username
        this.email = email
        this.password = password
    }

    constructor(id: String?, username: String?, email: String?, password: String?, roles: Set<MongoRole?>) {
        this.id = id
        this.email = email
        this.username = username
        this.password = password
        this.roles = roles
    }

    companion object {
        const val COLLECTION_NAME = "user"
    }
}
