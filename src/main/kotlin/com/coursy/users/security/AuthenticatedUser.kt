package com.coursy.users.security

import com.coursy.users.types.Email
import java.util.*

data class AuthenticatedUser(
    val email: Email,
    val tenantId: UUID?
)