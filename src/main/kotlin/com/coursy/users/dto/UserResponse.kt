package com.coursy.users.dto

import com.coursy.users.model.User
import com.coursy.users.types.Email
import com.coursy.users.types.Login

data class UserResponse(
    val id: Long,
    val email: Email,
    val login: Login,
    val roleName: String,
)

fun User.toUserResponse(): UserResponse {
    return UserResponse(
        id = this.id,
        email = this.email,
        login = this.login,
        roleName = this.role.name.name
    )
} 
