package io.github.anki.anki.exception

import java.util.*

class ErrorMessage(
    val statusCode: Int,
    val timestamp: Date,
    val message: String,
    val description: String)
