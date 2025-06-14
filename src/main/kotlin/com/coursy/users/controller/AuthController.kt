package com.coursy.users.controller

import arrow.core.flatMap
import arrow.core.left
import com.coursy.users.dto.LoginRequest
import com.coursy.users.dto.RefreshJwtRequest
import com.coursy.users.dto.RegistrationRequest
import com.coursy.users.failure.AuthorizationFailure
import com.coursy.users.model.RoleName
import com.coursy.users.service.AuthService
import com.coursy.users.service.UserService
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

    @PostMapping("/register/admin")
    fun createAdmin(@RequestBody request: RegistrationRequest): ResponseEntity<Any> {
        val result = request
            .copy(roleName = RoleName.ROLE_ADMIN.toString())
            .validate()

        return result.fold(
            { failure -> httpFailureResolver.handleFailure(failure) },
            { ResponseEntity.status(HttpStatus.CREATED).build() }
        )
    }

    private fun isRegistrationRolePermitted(request: RegistrationRequest.Validated) =
        request.roleName == RoleName.ROLE_STUDENT || request.roleName == RoleName.ROLE_TEACHER
}
