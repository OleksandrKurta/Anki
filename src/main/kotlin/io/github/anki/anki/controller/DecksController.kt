package io.github.anki.anki.controller

import io.github.anki.anki.service.DeckService
import org.springframework.web.bind.annotation.RestController

@RestController("api/v1/collection")
class DecksController(
    private val service: DeckService,
) {

//    @PostMapping
//    @ResponseStatus(HttpStatus.CREATED)
//    fun createCollection(@RequestBody request: NewCollectionRequest): CollectionDtoResponse {
//        val newCollection: Collection =
//    }

}