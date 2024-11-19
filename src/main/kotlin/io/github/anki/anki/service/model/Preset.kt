package io.github.anki.anki.service.model

import io.github.anki.anki.configuration.FileDefaultTemplate
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service


@Service
@Scope("prototype")
@FileDefaultTemplate(path = "src/main/resources/presetDefaultValues.properties")
data class Preset(
    var id: String? = null,
    var name: String = "default",
    val presetOwnerId: String? = null,
    var maxCardsPerDay: Int = 100,
    var learningSteps: ArrayList<String> = arrayListOf("10m", "20m"),
    var failedSteps: ArrayList<String> = arrayListOf("10m"),
)