package com.coursy.clientauthservice.dto

import com.coursy.clientauthservice.model.User
import com.coursy.clientauthservice.types.Email
import com.coursy.clientauthservice.types.Login

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
