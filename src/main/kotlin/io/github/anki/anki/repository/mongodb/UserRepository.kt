package io.github.anki.anki.repository.mongodb

import io.github.anki.anki.repository.mongodb.document.MongoUser
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.Optional

interface UserRepository : MongoRepository<MongoUser?, String?> {
    fun findByUsername(username: String?): Optional<MongoUser?>?

    fun existsByUsername(username: String?): Boolean?

    fun existsByEmail(email: String?): Boolean?
}
