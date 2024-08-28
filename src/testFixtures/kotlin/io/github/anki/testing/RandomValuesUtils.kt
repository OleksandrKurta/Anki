package io.github.anki.testing

import io.github.anki.anki.controller.dto.auth.SignUpRequestDto
import org.bson.types.ObjectId
import java.util.*

fun getRandomID(): ObjectId = ObjectId.get()

fun getRandomString(prefix: String = ""): String = prefix + UUID.randomUUID().toString()

fun getRandomEmail(prefix: String = ""): String = prefix + UUID.randomUUID().toString() + "@gmail.com"

fun SignUpRequestDto.Companion.randomUser(prefix: String = ""): SignUpRequestDto =
    SignUpRequestDto(
        email = getRandomEmail(prefix),
        userName = getRandomString(prefix),
        password = getRandomString(prefix),
        roles = setOf(),
    )
