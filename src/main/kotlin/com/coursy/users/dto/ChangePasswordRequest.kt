package com.coursy.users.dto

import arrow.core.Either
import com.coursy.users.failure.Failure
import com.coursy.users.types.Password

data class ChangePasswordRequest(
    val password: String
) : SelfValidating<Failure, ChangePasswordRequest.Validated> {
    data class Validated(
        val password: Password
    )

    override fun validate(): Either<Failure, Validated> {
        return Password.create(password)
            .map { password -> Validated(password = password) }
            .mapLeft { failure -> failure }
    }
}
