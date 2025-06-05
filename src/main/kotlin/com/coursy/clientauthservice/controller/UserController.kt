package com.coursy.clientauthservice.controller

import arrow.core.flatMap
import arrow.core.left
import com.coursy.clientauthservice.dto.RegistrationRequest
import com.coursy.clientauthservice.failure.AuthorizationFailure
import com.coursy.clientauthservice.model.RoleName
import com.coursy.clientauthservice.security.UserDetailsImp
import com.coursy.clientauthservice.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RequestMapping("/user")
@RestController
class UserController(
    private val userService: UserService,
    private val httpFailureResolver: HttpFailureResolver
) {
    @GetMapping("/me")
    fun getCurrentUser(@AuthenticationPrincipal currentUser: UserDetailsImp): ResponseEntity<Any> {
        return userService
            .getUser(currentUser.id)
            .fold(
                { failure -> httpFailureResolver.handleFailure(failure) },
                { response -> ResponseEntity.status(HttpStatus.OK).body(response) }
            )
    }

    @PostMapping
    fun createUser(@RequestBody request: RegistrationRequest): ResponseEntity<Any> {
        val result = request
            .validate()
            .flatMap { validated ->
                if (isOperationPermitted(validated))
                    userService.createUser(validated)
                else
                    AuthorizationFailure.InsufficientRole.left()
            }

        return result.fold(
            { failure -> httpFailureResolver.handleFailure(failure) },
            { ResponseEntity.status(HttpStatus.CREATED).build() }
        )
    }

    private fun isOperationPermitted(request: RegistrationRequest.Validated) =
        request.roleName == RoleName.ROLE_STUDENT || request.roleName == RoleName.ROLE_TEACHER
}