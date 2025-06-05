package com.coursy.clientauthservice.types

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.coursy.clientauthservice.failure.LoginTypeFailure

@JvmInline
value class Login private constructor(val value: String) {
    companion object {
        private const val MIN_LENGTH = 2
        private const val MAX_LENGTH = 50

        private fun isValidChar(c: Char): Boolean {
            return c.isLetterOrDigit() || c == ' ' || c == '-' || c == '\''
        }

        fun create(value: String): Either<LoginTypeFailure, Login> = when {
            value.isEmpty() -> LoginTypeFailure.Empty.left()
            value.length < MIN_LENGTH -> LoginTypeFailure.TooShort(MIN_LENGTH).left()
            value.length > MAX_LENGTH -> LoginTypeFailure.TooLong(MAX_LENGTH).left()
            !value.all { isValidChar(it) } -> LoginTypeFailure.InvalidFormat.left()
            else -> Login(value).right()
        }
    }
}
