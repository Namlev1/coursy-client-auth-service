package com.coursy.clientauthservice.controller

import arrow.core.flatMap
import arrow.core.left
import com.coursy.clientauthservice.dto.LoginRequest
import com.coursy.clientauthservice.dto.RefreshJwtRequest
import com.coursy.clientauthservice.dto.RegistrationRequest
import com.coursy.clientauthservice.failure.AuthorizationFailure
import com.coursy.clientauthservice.model.RoleName
import com.coursy.clientauthservice.service.AuthService
import com.coursy.clientauthservice.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val userService: UserService,
    private val authService: AuthService,
    private val httpFailureResolver: HttpFailureResolver
) {
    @PostMapping("/login")
    fun authenticateUser(@RequestBody request: LoginRequest): ResponseEntity<Any> {
        val result = request.validate().flatMap { validated -> authService.authenticateUser(validated) }

        return result.fold(
            { failure -> httpFailureResolver.handleFailure(failure) },
            { jwtResponse -> ResponseEntity.status(HttpStatus.OK).body(jwtResponse) }
        )
    }

    @GetMapping("/refresh")
    fun refreshJwt(@RequestBody request: RefreshJwtRequest): ResponseEntity<Any> {
        val result = request.validate().flatMap { validated -> authService.refreshJwtToken(validated) }

        return result.fold(
            { failure -> httpFailureResolver.handleFailure(failure) },
            { jwtResponse -> ResponseEntity.status(HttpStatus.OK).body(jwtResponse) }
        )
    }

    @PostMapping("/register")
    fun createUser(@RequestBody request: RegistrationRequest): ResponseEntity<Any> {
        val result = request
            .validate()
            .flatMap { validated ->
                if (isRegistrationRolePermitted(validated))
                    userService.createUser(validated)
                else
                    AuthorizationFailure.InsufficientRole.left()
            }

        return result.fold(
            { failure -> httpFailureResolver.handleFailure(failure) },
            { ResponseEntity.status(HttpStatus.CREATED).build() }
        )
    }

    private fun isRegistrationRolePermitted(request: RegistrationRequest.Validated) =
        request.roleName == RoleName.ROLE_STUDENT || request.roleName == RoleName.ROLE_TEACHER
}
