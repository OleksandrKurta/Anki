package io.github.anki.testing

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.test.web.reactive.server.WebTestClient

inline fun <reified T> WebTestClient.ResponseSpec.getDtoFromResponseBody(mapper: ObjectMapper): T =
    this.expectBody()
        .returnResult()
        .responseBody!!
        .decodeToString()
        .let { mapper.readValue(it, T::class.java) }

inline fun <reified T> WebTestClient.ResponseSpec.getListOfDtoFromResponseBody(mapper: ObjectMapper): List<T> =
    this.expectBody()
        .returnResult()
        .responseBody!!
        .decodeToString()
        .let { mapper.readValue(it, mapper.typeFactory.constructCollectionType(List::class.java, T::class.java)) }
