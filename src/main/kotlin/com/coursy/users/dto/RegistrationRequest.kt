package com.coursy.users.dto

import arrow.core.Either
import arrow.core.Either.Companion.catch
import arrow.core.raise.either
import arrow.core.right
import com.coursy.users.failure.Failure
import com.coursy.users.failure.RoleFailure
import com.coursy.users.model.Role
import com.coursy.users.types.Email
import com.coursy.users.types.Name
import com.coursy.users.types.Password

data class RegistrationRequest(
    val email: String,
    val firstName: String,
    val lastName: String,
    val password: String,
    val roleName: String
) : SelfValidating<Failure, RegistrationRequest.Validated> {
    data class Validated(
        val email: Email,
        val firstName: Name,
        val lastName: Name,
        val password: Password,
        val roleName: Role
    )

    override fun validate(): Either<Failure, Validated> {
        return either {
            val emailResult = Email.create(email).bind()
            val passwordResult = Password.create(password).bind()
            val firstNameResult = Name.create(firstName).bind()
            val lastNameResult = Name.create(lastName).bind()
            val roleNameResult = catch { Role.valueOf(roleName) }
                .mapLeft { RoleFailure.NotFound }
                .bind()

            return Validated(
                emailResult,
                firstNameResult,
                lastNameResult,
                passwordResult,
                roleNameResult
            ).right()
        }
    }
}
