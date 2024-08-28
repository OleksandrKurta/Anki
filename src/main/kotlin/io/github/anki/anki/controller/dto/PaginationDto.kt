package io.github.anki.anki.controller.dto

data class PaginationDto(
    val limit: Int = DEFAULT_LIMIT,
    val offset: Int = 0,
) {
    companion object {
        const val DEFAULT_LIMIT: Int = 50
    }
}
