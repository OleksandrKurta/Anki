package io.github.anki.anki.controller.dto.auth

import com.fasterxml.jackson.annotation.JsonProperty

data class UserCreatedMessageResponseDto(
    @JsonProperty("message") val message: String,
)
