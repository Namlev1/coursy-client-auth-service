package com.coursy.clientauthservice.integration.userController

import com.coursy.clientauthservice.dto.RegistrationRequest
import com.coursy.clientauthservice.model.RoleName

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
}