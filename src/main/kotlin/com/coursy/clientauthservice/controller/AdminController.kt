package com.coursy.clientauthservice.controller

import arrow.core.flatMap
import arrow.core.left
import com.coursy.clientauthservice.dto.RegistrationRequest
import com.coursy.clientauthservice.failure.AuthorizationFailure
import com.coursy.clientauthservice.model.RoleName
import com.coursy.clientauthservice.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/admin")
@RestController
class AdminController(
    private val userService: UserService,
    private val httpFailureResolver: HttpFailureResolver
) {
    @PostMapping
    fun createUser(@RequestBody request: RegistrationRequest): ResponseEntity<Any> {
        val result = request
            .validate()
            .flatMap { validated ->
                if (validated.roleName == RoleName.ROLE_SUPER_ADMIN)
                    AuthorizationFailure.InsufficientRole.left()
                else
                    userService.createUser(validated)
            }

        return result.fold(
            { failure -> httpFailureResolver.handleFailure(failure) },
            { ResponseEntity.status(HttpStatus.CREATED).build() }
        )
    }
}