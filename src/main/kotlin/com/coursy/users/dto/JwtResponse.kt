package com.coursy.users.dto

data class JwtResponse(
    val token: String,
    val refreshToken: String,
)
