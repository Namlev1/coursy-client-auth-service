package com.coursy.users.failure

sealed class LoginTypeFailure : Failure {
    data object Empty : LoginTypeFailure()
    data object InvalidFormat : LoginTypeFailure()
    data class TooShort(val minLength: Int) : LoginTypeFailure()
    data class TooLong(val maxLength: Int) : LoginTypeFailure()

    override fun message(): String = when (this) {
        Empty -> "Login cannot be empty"
        InvalidFormat -> "Login format is invalid"
        is TooLong -> "Login is too long (maximum length: $maxLength)"
        is TooShort -> "Login is too short (minimum length: $minLength)"
    }
}
