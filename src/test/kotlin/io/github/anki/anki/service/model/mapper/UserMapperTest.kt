package io.github.anki.anki.service.model.mapper

import io.github.anki.anki.controller.dto.auth.JwtResponseDto
import io.github.anki.anki.repository.mongodb.document.MongoUser
import io.github.anki.anki.repository.mongodb.document.Role
import io.github.anki.anki.service.model.User
import io.github.anki.anki.service.secure.UserAuthentication
import io.github.anki.testing.getRandomID
import io.github.anki.testing.getRandomString
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import org.bson.types.ObjectId
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.springframework.security.core.authority.SimpleGrantedAuthority
import kotlin.test.BeforeTest
import kotlin.test.Test

class UserMapperTest {

    private lateinit var randomUserId: ObjectId
    private lateinit var randomUserName: String
    private lateinit var randomUserEmail: String
    private lateinit var randomUserPassword: String

    @BeforeTest
    fun setUp() {
        randomUserId = getRandomID()
        randomUserName = getRandomString()
        randomUserEmail = getRandomString()
        randomUserPassword = getRandomString()
    }

    @Nested
    @DisplayName("User.toMongoUser()")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class UserToMongoUser {
        @Test
        fun `should map User to MongoUser when id is null`() {
            // given
            val user =
                User(
                    id = null,
                    userName = randomUserName,
                    email = randomUserEmail,
                    password = randomUserPassword,
                    authorities = listOf(SimpleGrantedAuthority(Role.ROLE_USER.name)),
                )
            val expectedMongoUser =
                MongoUser(
                    id = null,
                    userName = randomUserName,
                    email = randomUserEmail,
                    password = randomUserPassword,
                    roles = setOf(Role.ROLE_USER.name),
                )

            // when
            val actualMongoUser = user.toMongoUser()

            // then
            actualMongoUser shouldBe expectedMongoUser

            actualMongoUser.id shouldBe null
        }
    }

    @Nested
    @DisplayName("MongoUser.toUser()")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class MongoUserToUser {
        @Test
        fun `should map MongoUser to User`() {
            // given
            val mongoUser =
                MongoUser(
                    id = randomUserId,
                    userName = randomUserName,
                    email = randomUserEmail,
                    password = randomUserPassword,
                    roles = setOf(Role.ROLE_USER.name),
                )

            val expectedUser =
                User(
                    id = randomUserId.toString(),
                    userName = randomUserName,
                    email = randomUserEmail,
                    password = randomUserPassword,
                    authorities = setOf(SimpleGrantedAuthority(Role.ROLE_USER.name)),
                )

            // when
            val actualUser = mongoUser.toUser()

            // then
            actualUser shouldBe expectedUser

            actualUser.id shouldBe expectedUser.id
        }

        @Test
        fun `should raise IllegalArgumentException when no roles`() {
            val mongoUser =
                MongoUser(
                    id = randomUserId,
                    userName = randomUserName,
                    email = randomUserEmail,
                    password = randomUserPassword,
                    roles = setOf(null),
                )

            // when
            shouldThrowExactly<IllegalArgumentException> { mongoUser.toUser() }
        }
    }

    @Nested
    @DisplayName("User.toJwtDto(token)")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class UserAuthenticationToJwtDto {
        @Test
        fun `should map MongoUser to User`() {
            // given
            val token = getRandomString()
            val user =
                User(
                    id = randomUserId.toString(),
                    userName = randomUserName,
                    email = randomUserEmail,
                    password = randomUserPassword,
                    authorities = listOf(SimpleGrantedAuthority(Role.ROLE_USER.name)),
                )

            val expectedJwtDto =
                JwtResponseDto(
                    accessToken = token,
                    id = randomUserId.toString(),
                    email = randomUserEmail,
                    userName = randomUserName,
                    roles = setOf(Role.ROLE_USER.name),
                )

            val userAuthentication = UserAuthentication(user, token)

            // when
            val actualJwtDto = userAuthentication.toJwtDto()

            // then
            actualJwtDto shouldBe expectedJwtDto
        }
    }
}
