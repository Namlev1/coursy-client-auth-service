package com.coursy.clientauthservice.integration.userController

import com.coursy.clientauthservice.dto.RegistrationRequest
import com.coursy.clientauthservice.model.RoleName
import com.coursy.clientauthservice.security.UserDetailsImp
import com.coursy.clientauthservice.types.Email
import com.coursy.clientauthservice.types.Login
import org.springframework.security.core.authority.SimpleGrantedAuthority

class UserTestFixtures {
    val userUrl = "/v1/users"
    val authUrl = "/v1/auth"
    val registrationLogin = "registration login"
    val registrationEmail = "registration@email.com"
    val registrationPassword = "Str0ngPassw0RD!!"
    val registrationRole = RoleName.ROLE_STUDENT.name
    val registrationRequest = RegistrationRequest(
        registrationLogin,
        registrationEmail,
        registrationPassword,
        registrationRole
    )

    val adminDetails = UserDetailsImp(
        id = 1L,
        email = Email.create("testuser@example.com").getOrNull()!!,
        authorities = mutableSetOf(SimpleGrantedAuthority("ROLE_ADMIN")),
        password = "password",
        login = Login.create("testuser").getOrNull()!!,
        enabled = true,
        accountNonLocked = true
    )
}