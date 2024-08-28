package io.github.anki.anki.controller.dto.mapper

import io.github.anki.anki.controller.dto.auth.SignInRequestDto
import io.github.anki.anki.controller.dto.auth.SignUpRequestDto
import io.github.anki.anki.repository.mongodb.document.Role
import io.github.anki.anki.service.model.User
import io.github.anki.testing.getRandomID
import io.github.anki.testing.getRandomString
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.shouldBe
import org.bson.types.ObjectId
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.springframework.security.core.GrantedAuthority
import java.util.stream.Collectors
import kotlin.test.BeforeTest
import kotlin.test.Test

@Tag("unit")
class AuthDtoMapperTest {
    private lateinit var randomID: ObjectId
    private lateinit var randomUserName: String
    private lateinit var randomEmail: String
    private lateinit var randomPassword: String

    @BeforeTest
    fun setUp() {
        randomID = getRandomID()
        randomUserName = getRandomString()
        randomEmail = getRandomString()
        randomPassword = getRandomString()
    }

    @Nested
    @DisplayName("SignInRequestDto.toUser()")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class SignInRequestDtoToUser {
        @Test
        fun `should map SignInRequestDto to User`() {
            // GIVEN
            val signInRequestDto =
                SignInRequestDto(
                    userName = randomUserName,
                    password = randomPassword,
                )
            val expectedUser =
                User(
                    userName = randomUserName,
                    password = randomPassword,
                )

            // WHEN
            val actual: User = signInRequestDto.toUser()

            // THEN
            actual.shouldBeEqualToIgnoringFields(expectedUser, User::id)

            actual.id shouldBe null
        }
    }

    @Nested
    @DisplayName("SignUpRequestDto.toUser")
    @TestInstance(Lifecycle.PER_CLASS)
    inner class SignUpRequestDtoToUser {
        @Test
        fun `should map SignUpRequestDto to User`() {
            // GIVEN
            val signUpRequestDto =
                SignUpRequestDto(
                    userName = randomUserName,
                    password = randomPassword,
                    email = randomEmail,
                    roles = setOf("ROLE_USER"),
                )
            val expectedUser =
                User(
                    userName = randomUserName,
                    password = randomPassword,
                    email = randomEmail,
                    authorities =
                    listOf(Role.ROLE_USER.name).stream().map { role ->
                        GrantedAuthority {
                            Role.valueOf(
                                role,
                            ).name
                        }
                    }.collect(Collectors.toList()),
                )

            // WHEN
            val actual: User = signUpRequestDto.toUser(signUpRequestDto.password)

            // THEN
            actual.shouldBeEqualToIgnoringFields(expectedUser, User::id)

            actual.id shouldBe null
        }
    }
}
