package io.github.anki.anki.controller

import io.github.anki.anki.controller.DecksController.Companion.CONCRETE_DECK
import io.github.anki.anki.controller.dto.*
import io.github.anki.anki.controller.dto.mapper.toDto
import io.github.anki.anki.controller.dto.mapper.toPreset
import io.github.anki.anki.service.PresetService
import io.github.anki.anki.service.secure.SecurityService
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(PresetController.BASE_URL)
class PresetController @Autowired constructor(
    val presetService: PresetService,
    val securityService: SecurityService,
) {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getPresets(@RequestHeader header: HttpHeaders): List<PresetDtoResponse> {
        LOG.info("IN: $PresetController ${PresetController.BASE_URL} get presets")
        val presets = presetService.getPresets(securityService.jwtUtils.getUserIdFromAuthHeader(header))
        LOG.info("OUT: $PresetController ${PresetController.BASE_URL} return all user presets")
        return presets.map { it.toDto() }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createPreset(
        @RequestHeader header: HttpHeaders,
        @RequestBody presetDto: @Valid NewPresetDto,
    ): PresetDtoResponse {
        LOG.info("IN: $PresetController ${PresetController.BASE_URL} create preset ${presetDto.name}")
        val preset =
            presetService.createNewPreset(
                presetDto.toPreset(securityService.jwtUtils.getUserIdFromAuthHeader(header)),
            )
        LOG.info("OUT: $PresetController ${PresetController.BASE_URL} create deck with id = ${preset.id}")
        return preset.toDto()
    }

    @PatchMapping(CONCRETE_PRESET)
    @ResponseStatus(HttpStatus.OK)
    fun patchPreset(
        @Valid @RequestBody request: PatchPresetDto,
        @PathVariable presetId: String,
        @RequestHeader header: HttpHeaders,
    ): PresetDtoResponse {
        LOG.info("IN: $PresetController ${PresetController.BASE_URL} patch $request preset with id = $presetId")
        val preset =
            presetService.updatePreset(
                request.toPreset(presetId, securityService.jwtUtils.getUserIdFromAuthHeader(header)),
            )
        LOG.info("OUT: $PresetController ${PresetController.BASE_URL} patched deck with id = $presetId")
        return preset.toDto()
    }

    @DeleteMapping(CONCRETE_DECK)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deletePreset(@PathVariable presetId: String, @RequestHeader header: HttpHeaders) {
        LOG.info("IN: $PresetController ${PresetController.BASE_URL} delete deck with id = $presetId")
        val preset = presetService.deletePreset(presetId, securityService.jwtUtils.getUserIdFromAuthHeader(header))
        LOG.info("OUT: $PresetController ${PresetController.BASE_URL} deleted deck with id = $presetId")
        return preset
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(PresetController::class.java)
        const val BASE_URL = "/api/v1/remindPreset/"
        const val CONCRETE_PRESET = "/{presetId}"
    }
}
