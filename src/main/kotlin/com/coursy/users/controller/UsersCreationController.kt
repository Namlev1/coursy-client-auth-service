package com.coursy.users.controller

import arrow.core.flatMap
import com.coursy.users.dto.RegistrationRequest
import com.coursy.users.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.web.bind.annotation.*
import java.util.*

@RequestMapping("/api/users")
@RestController
class UsersCreationController(
    private val userService: UserService,
    private val httpFailureResolver: HttpFailureResolver
) {

    @PostMapping("/host/register")
    fun createHostUser(
        @RequestBody request: RegistrationRequest,
        jwt: PreAuthenticatedAuthenticationToken?
    ): ResponseEntity<Any> {
        return createUser(null, request, jwt)
    }

    @PostMapping("/platform/{platformId}/register")
    fun createPlatformUser(
        @PathVariable platformId: UUID,
        @RequestBody request: RegistrationRequest,
        jwt: PreAuthenticatedAuthenticationToken?
    ): ResponseEntity<Any> {
        return createUser(platformId, request, jwt)
    }

    private fun createUser(
        @PathVariable platformId: UUID?,
        @RequestBody request: RegistrationRequest,
        jwt: PreAuthenticatedAuthenticationToken?
    ): ResponseEntity<Any> {
        val result = request
            .validate()
            .flatMap { validated ->
                userService.createPlatformUser(validated, platformId, jwt)
            }

        return result.fold(
            { failure -> httpFailureResolver.handleFailure(failure) },
            { ResponseEntity.status(HttpStatus.CREATED).build() }
        )
    }

}