package com.coursy.clientauthservice.dto

import arrow.core.Either
import com.coursy.clientauthservice.failure.Failure
import com.coursy.clientauthservice.types.Password

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
