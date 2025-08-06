package com.coursy.users.integration.userController

import com.coursy.users.dto.RegistrationRequest
import com.coursy.users.model.Role
import com.coursy.users.types.Email
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken

class UserTestFixtures {
    val userUrl = "/api/users"
    val authUrl = "/api/auth"
    val registrationLogin = "registration login"
    val registrationEmail = "registration@email.com"
    val registrationPassword = "Str0ngPassw0RD!!"
    val registrationRole = Role.ROLE_STUDENT.name
    val registrationRequest = RegistrationRequest(
        registrationLogin,
        registrationEmail,
        registrationPassword,
        registrationRole
    )

    val studentToken = PreAuthenticatedAuthenticationToken(
        Email.create("testuser@example.com").getOrNull()!!,
        null,
        mutableSetOf(SimpleGrantedAuthority("ROLE_STUDENT"))
    ).apply { isAuthenticated = true }

    val adminToken = PreAuthenticatedAuthenticationToken(
        Email.create("testuser@example.com").getOrNull()!!,
        null,
        mutableSetOf(SimpleGrantedAuthority("ROLE_ADMIN"))
    ).apply { isAuthenticated = true }

    val superAdminToken = PreAuthenticatedAuthenticationToken(
        Email.create("testuser@example.com").getOrNull()!!,
        null,
        mutableSetOf(SimpleGrantedAuthority("ROLE_SUPER_ADMIN"))
    ).apply { isAuthenticated = true }
}