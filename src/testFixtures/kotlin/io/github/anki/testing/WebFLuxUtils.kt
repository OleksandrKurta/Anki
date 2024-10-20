package io.github.anki.testing

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.test.web.reactive.server.WebTestClient

inline fun <reified T> WebTestClient.ResponseSpec.getAndMapResponseBody(): T =
    this.expectBody()
        .returnResult()
        .responseBody!!
        .let { ObjectMapper().readValue(it, T::class.java) }
