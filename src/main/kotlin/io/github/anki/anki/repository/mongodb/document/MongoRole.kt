package io.github.anki.anki.repository.mongodb.document

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document


@Document(collection = MongoRole.COLLECTION_NAME)
class MongoRole {
    @Id
    var id: String? = null
    private var name: ERole? = null

    constructor()

    constructor(name: ERole?) {
        this.name = name
    }

    fun getName(): ERole? {
        return name
    }

    fun setName(name: ERole?) {
        this.name = name
    }
    companion object {
        const val COLLECTION_NAME = "role"
    }
}