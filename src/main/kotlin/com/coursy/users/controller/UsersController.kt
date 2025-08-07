package com.coursy.users.controller

import arrow.core.flatMap
import com.coursy.users.dto.RoleUpdateRequest
import com.coursy.users.service.UserService
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.web.bind.annotation.*
import java.util.*

@RequestMapping("/api/users/")
@RestController
class UsersController(
    private val userService: UserService,
    private val httpFailureResolver: HttpFailureResolver
) {

//    @PostMapping("/register")
//    fun createUser(
//        @PathVariable platformId: UUID,
//        @RequestBody request: RegistrationRequest,
//        jwt: PreAuthenticatedAuthenticationToken?
//    ): ResponseEntity<Any> {
//        val result = request
//            .validate()
//            .flatMap { validated ->
//                userService.createPlatformUser(validated, platformId, jwt)
//            }
//
//        return result.fold(
//            { failure -> httpFailureResolver.handleFailure(failure) },
//            { ResponseEntity.status(HttpStatus.CREATED).build() }
//        )
//    }

    @GetMapping("/me")
    fun getCurrentUser(
        jwt: PreAuthenticatedAuthenticationToken
    ): ResponseEntity<Any> {
        return userService
            .getPrincipalUser(jwt)
            .fold(
                { failure -> httpFailureResolver.handleFailure(failure) },
                { response -> ResponseEntity.status(HttpStatus.OK).body(response) }
            )
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    fun getUser(@PathVariable id: UUID, jwt: PreAuthenticatedAuthenticationToken) = userService
        .getUser(jwt, id)
        .fold(
            { failure -> httpFailureResolver.handleFailure(failure) },
            { response -> ResponseEntity.status(HttpStatus.OK).body(response) }
        )

    @PreAuthorize("hasAnyRole('HOST_OWNER', 'HOST_ADMIN', 'PLATFORM_OWNER', 'PLATFORM_ADMIN', 'PLATFORM_USER')")
    @GetMapping
    fun getUserList(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        jwt: PreAuthenticatedAuthenticationToken
    ): ResponseEntity<Any> {
        if (!arePageParamsInvalid(page, size))
            return ResponseEntity.badRequest().build()
        val request = PageRequest.of(page, size)
        return userService
            .getUserPage(jwt, request)
            .fold(
                { failure -> httpFailureResolver.handleFailure(failure) },
                { ResponseEntity.ok(it) }
            )

    }

    @PreAuthorize("hasAnyRole('HOST_OWNER', 'HOST_ADMIN', 'PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    @PutMapping("/{id}")
    fun updateUserRole(
        @PathVariable id: UUID,
        @RequestBody request: RoleUpdateRequest,
        jwt: PreAuthenticatedAuthenticationToken
    ): ResponseEntity<Any> {
        val result = request
            .validate()
            .flatMap { validated ->
                userService.updateUserRole(id, validated, jwt)
            }

        return result.fold(
            { failure -> httpFailureResolver.handleFailure(failure) },
            { response -> ResponseEntity.status(HttpStatus.OK).body(response) }
        )
    }


    @PreAuthorize("hasAnyRole('HOST_OWNER', 'HOST_ADMIN', 'PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    @DeleteMapping("/{id}")
    fun deleteUser(
        @PathVariable id: UUID,
        jwt: PreAuthenticatedAuthenticationToken
    ): ResponseEntity<Any> {
        return userService.removeUser(id, jwt)
            .fold(
                { failure -> httpFailureResolver.handleFailure(failure) },
                { ResponseEntity.status(HttpStatus.NO_CONTENT).build() }
            )
    }

    private fun arePageParamsInvalid(page: Int, size: Int) =
        page < 0 || size <= 0
}