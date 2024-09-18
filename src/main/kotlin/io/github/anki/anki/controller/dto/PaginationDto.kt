package io.github.anki.anki.controller.dto

data class PaginationDto(
    val limit: Int = DEFAULT_LIMIT,
    val offset: Int = DEFAULT_OFFSET,
) {
    companion object {
        const val DEFAULT_LIMIT: Int = 50
        const val DEFAULT_OFFSET: Int = 0
        const val LIMIT: String = "limit"
        const val OFFSET: String = "offset"
    }
}
