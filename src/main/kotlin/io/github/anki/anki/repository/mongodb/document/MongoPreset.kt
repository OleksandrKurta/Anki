package io.github.anki.anki.repository.mongodb.document

import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.Instant

@Document(collection = MongoPreset.COLLECTION_NAME)
data class MongoPreset(
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
    @Field(PRESET_NAME)
    val presetName: String,
    @Field(PRESET_OWNER_ID)
    val presetOwnerId: ObjectId,
    @Field(MAX_CARDS_PER_DAY)
    val maxCardsPerDay: Int,
    @Field(LEARNING_STEPS)
    val learningSteps: ArrayList<String>,
    @Field(FAILED_STEPS)
    val failedSteps: ArrayList<String>,
    @Field(DEFAULT)
    val defaultPreset: Boolean = false,
) : MongoDocument {
    companion object {
        const val COLLECTION_NAME = "presets"
        const val PRESET_NAME = "presetName"
        const val PRESET_OWNER_ID = "presetOwnerId"
        const val MAX_CARDS_PER_DAY = "maxCardsPerDay"
        const val LEARNING_STEPS = "learningSteps"
        const val FAILED_STEPS = "failedSteps"
        const val DEFAULT = "defaultPreset"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MongoPreset

        if (id != other.id) return false
        if (status != other.status) return false
        if (presetName != other.presetName) return false
        if (presetOwnerId != other.presetOwnerId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + status.hashCode()
        result = 31 * result + presetName.hashCode()
        result = 31 * result + presetOwnerId.hashCode()
        return result
    }
}
