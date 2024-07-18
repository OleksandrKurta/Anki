package io.github.anki.anki.models

import org.springframework.stereotype.Repository


@Repository
data class CardCollection(
    val id: Int,
    val userId: Int,
    val name: String,
    val description: String,
)
