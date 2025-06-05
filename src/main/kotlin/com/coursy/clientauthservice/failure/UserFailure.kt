package com.coursy.clientauthservice.failure

sealed class UserFailure : Failure {
    data object EmailAlreadyExists : UserFailure()
    data object LoginAlreadyExists : UserFailure()
    data object IdNotExists : UserFailure()

    override fun message(): String = when (this) {
        EmailAlreadyExists -> "User with this email already exists."
        LoginAlreadyExists -> "User with this login already exists."
        IdNotExists -> "User with this id does not exist."
    }
}
