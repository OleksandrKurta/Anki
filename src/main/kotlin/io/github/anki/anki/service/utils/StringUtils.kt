package io.github.anki.anki.service.utils

import org.bson.types.ObjectId

fun String.toObjectId() = ObjectId(this)
