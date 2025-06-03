package com.coursy.clientauthservice.dto

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.coursy.clientauthservice.failure.Failure
import com.coursy.clientauthservice.failure.RefreshTokenFailure

data class RefreshJwtRequest(
    val refreshToken: String
) : SelfValidating<Failure, RefreshJwtRequest.Validated> {

    data class Validated(
        val refreshToken: String
    )

    override fun validate(): Either<Failure, Validated> {
        if (refreshToken.isBlank())
            return RefreshTokenFailure.Empty.left()

        return Validated(refreshToken).right()
    }
}
