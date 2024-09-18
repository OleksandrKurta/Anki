package io.github.anki.anki.controller.dto.mapper

import io.github.anki.anki.controller.dto.PaginationDto
import io.github.anki.anki.service.model.Pagination

fun PaginationDto.toPagination() = Pagination(this.limit, this.offset)
