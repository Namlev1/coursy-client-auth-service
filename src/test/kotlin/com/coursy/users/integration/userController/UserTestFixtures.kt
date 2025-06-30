package com.coursy.users.integration.userController

import com.coursy.users.dto.RegistrationRequest
import com.coursy.users.model.RoleName
import com.coursy.users.security.UserDetailsImp
import com.coursy.users.types.Email
import com.coursy.users.types.Login
import org.springframework.security.core.authority.SimpleGrantedAuthority

class UserTestFixtures {
    val userUrl = "/api/users"
    val authUrl = "/api/auth"
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

    val studentDetails = UserDetailsImp(
        id = 1L,
        email = Email.create("testuser@example.com").getOrNull()!!,
        authorities = mutableSetOf(SimpleGrantedAuthority("ROLE_STUNENT")),
        password = "password",
        login = Login.create("testuser").getOrNull()!!,
        enabled = true,
        accountNonLocked = true
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

    val superAdmirDetails = UserDetailsImp(
        id = 1L,
        email = Email.create("testuser@example.com").getOrNull()!!,
        authorities = mutableSetOf(SimpleGrantedAuthority("ROLE_SUPER_ADMIN")),
        password = "password",
        login = Login.create("testuser").getOrNull()!!,
        enabled = true,
        accountNonLocked = true
    )
}