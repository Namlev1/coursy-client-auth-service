package com.coursy.users.controller

import arrow.core.Either
import arrow.core.flatMap
import com.coursy.users.dto.RegistrationRequest
import com.coursy.users.failure.Failure
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
        return createUser(request) { validated ->
            userService.createHostUser(validated, jwt)
        }
    }

    @PostMapping("/platform/{platformId}/register")
    fun createPlatformUser(
        @PathVariable platformId: UUID,
        @RequestBody request: RegistrationRequest,
        jwt: PreAuthenticatedAuthenticationToken?
    ): ResponseEntity<Any> {
        return createUser(request) { validated ->
            userService.createPlatformUser(validated, platformId, jwt)
        }
    }

    private fun createUser(
        request: RegistrationRequest,
        serviceCall: (RegistrationRequest.Validated) -> Either<Failure, Unit>
    ): ResponseEntity<Any> {
        val result = request
            .validate()
            .flatMap { validated ->
                serviceCall(validated)
            }

        return result.fold(
            { failure -> httpFailureResolver.handleFailure(failure) },
            { ResponseEntity.status(HttpStatus.CREATED).build() }
        )
    }
}