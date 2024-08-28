package io.github.anki.anki.repository.mongodb.document

import org.bson.types.ObjectId
import java.time.Instant

interface MongoDocument {
    val id: ObjectId?
    val createdAt: Instant?
    val modifiedAt: Instant?
    val status: DocumentStatus

    companion object {
        const val ID = "id"
        const val CREATED_AT = "createdAt"
        const val MODIFIED_AT = "modifiedAt"
        const val DOCUMENT_STATUS = "status"
    }
}
