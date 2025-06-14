package com.coursy.users.dto

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.coursy.users.failure.Failure
import com.coursy.users.failure.RoleFailure
import com.coursy.users.model.RoleName

data class RoleUpdateRequest(
    val roleName: String
) : SelfValidating<Failure, RoleUpdateRequest.Validated> {
    data class Validated(
        val roleName: RoleName,
    )

    override fun validate(): Either<Failure, Validated> {
        val role = RoleName.entries
            .find { it.name.equals(roleName, ignoreCase = true) }
            ?: return RoleFailure.NotFound.left()

        return Validated(role).right()
    }
}
