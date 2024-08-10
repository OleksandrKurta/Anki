package io.github.anki.testing

import org.bson.types.ObjectId
import java.util.*

fun getRandomID(): ObjectId = ObjectId.get()

fun getRandomString(prefix: String = ""): String = prefix + UUID.randomUUID().toString()
