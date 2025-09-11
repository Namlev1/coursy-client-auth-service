package com.coursy.users.dto

import com.coursy.users.model.User
import com.coursy.users.types.Email
import com.coursy.users.types.Name
import java.util.*

data class UserResponse(
    val id: UUID,
    val platformId: UUID?,
    val email: Email,
    val firstName: Name,
    val lastName: Name,
    val roleName: String,
)

fun User.toUserResponse(): UserResponse {
    return UserResponse(
        id = this.id,
        platformId = this.platformId,
        email = this.email,
        roleName = this.role.name,
        firstName = this.firstName,
        lastName = this.lastName
    )
} 
