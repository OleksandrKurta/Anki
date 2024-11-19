package io.github.anki.anki.service.exceptions

class PresetNotExistException(
    override val message: String = "Preset does not exist!",
    override val cause: Throwable? = null,
) : BaseBadRequestException, RuntimeException(message, cause) {

    companion object {
        fun fromPresetId(presetId: String) =
            PresetNotExistException("Preset with id = $presetId does not exist!")
    }
}
