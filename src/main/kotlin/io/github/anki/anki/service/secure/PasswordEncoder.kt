package io.github.anki.anki.service.secure

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class PasswordEncoder : BCryptPasswordEncoder()
