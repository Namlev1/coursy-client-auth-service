package com.coursy.clientauthservice.failure

sealed class RoleFailure : Failure {
    data object NotFound : RoleFailure()
    data object IsNull : RoleFailure()

    override fun message(): String = when (this) {
        NotFound -> "Role not found"
        IsNull -> "Role cannot be null"
    }
}
