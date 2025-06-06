package com.coursy.clientauthservice.controller

import arrow.core.flatMap
import com.coursy.clientauthservice.dto.ChangePasswordRequest
import com.coursy.clientauthservice.dto.RoleUpdateRequest
import com.coursy.clientauthservice.security.UserDetailsImp
import com.coursy.clientauthservice.service.UserService
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RequestMapping("/users")
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

    @PutMapping("/me/password")
    fun updateCurrentUserPassword(
        @AuthenticationPrincipal currentUser: UserDetailsImp,
        @RequestBody request: ChangePasswordRequest
    ): ResponseEntity<Any> {
        val result = request
            .validate()
            .flatMap { validated ->
                userService.updatePassword(currentUser.id, validated)
            }

        return result.fold(
            { failure -> httpFailureResolver.handleFailure(failure) },
            { ResponseEntity.status(HttpStatus.OK).build() }
        )
    }

    @GetMapping
    fun getUserList(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ): ResponseEntity<Any> =
        when {
            arePageParamsInvalid(page, size) -> ResponseEntity.badRequest().build()
            else -> PageRequest.of(page, size)
                .let { userService.getUserPage(it) }
                .let { ResponseEntity.ok(it) }
        }

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long) = userService
        .getUser(id)
        .fold(
            { failure -> httpFailureResolver.handleFailure(failure) },
            { response -> ResponseEntity.status(HttpStatus.OK).body(response) }
        )

    // todo: admin can't grand super-admins
    @PutMapping("/{id}")
    fun updateUserRole(
        @PathVariable id: Long,
        @RequestBody request: RoleUpdateRequest
    ): ResponseEntity<Any> {
        val result = request
            .validate()
            .flatMap { validated ->
                userService.updateUserRole(id, validated)
            }

        return result.fold(
            { failure -> httpFailureResolver.handleFailure(failure) },
            { response -> ResponseEntity.status(HttpStatus.OK).body(response) }
        )
    }


    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long) = userService
        .removeUser(id = id)
        .fold(
            { failure -> httpFailureResolver.handleFailure(failure) },
            { ResponseEntity.status(HttpStatus.NO_CONTENT).build() }
        )

    private fun arePageParamsInvalid(page: Int, size: Int) =
        page < 0 || size <= 0
}