package com.coursy.clientauthservice.dto

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.coursy.clientauthservice.failure.Failure
import com.coursy.clientauthservice.failure.RoleFailure
import com.coursy.clientauthservice.model.RoleName
import com.coursy.clientauthservice.types.Email
import com.coursy.clientauthservice.types.Login
import com.coursy.clientauthservice.types.Password

data class RegistrationRequest(
    val login: String,
    val email: String,
    val password: String,
    val roleName: String
) : SelfValidating<Failure, RegistrationRequest.Validated> {
    data class Validated(
        val login: Login,
        val email: Email,
        val password: Password,
        val roleName: RoleName
    )

    override fun validate(): Either<Failure, Validated> {
        val loginResult = Login.create(this@RegistrationRequest.login)
        val emailResult = Email.create(email)
        val passwordResult = Password.create(password)
        val roleNameResult = RoleName.entries
            .find { it.name.equals(roleName, ignoreCase = true) }?.right()
            ?: RoleFailure.NotFound.left()

        val firstError = listOfNotNull(
            loginResult.leftOrNull(),
            emailResult.leftOrNull(),
            passwordResult.leftOrNull(),
            roleNameResult.leftOrNull()
        ).firstOrNull()

        return firstError?.left() ?: Validated(
            login = loginResult.getOrNull()!!,
            email = emailResult.getOrNull()!!,
            password = passwordResult.getOrNull()!!,
            roleName = roleNameResult.getOrNull()!!
        ).right()
    }
}
