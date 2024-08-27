package io.github.anki.anki.repository.mongodb

import io.github.anki.anki.repository.mongodb.document.MongoRole
import io.github.anki.anki.repository.mongodb.document.Role
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.Optional

interface RoleRepository : MongoRepository<MongoRole?, String?> {
    fun findByName(name: Role?): Optional<MongoRole?>?
}