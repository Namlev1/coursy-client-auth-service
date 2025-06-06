package com.coursy.clientauthservice.controller

import arrow.core.flatMap
import com.coursy.clientauthservice.dto.ChangePasswordRequest
import com.coursy.clientauthservice.security.UserDetailsImp
import com.coursy.clientauthservice.service.UserService
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

    @GetMapping("/")
    fun getUserList(): Nothing = TODO()


    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long): Nothing = TODO()

    @PutMapping("/{id}")
    fun updateUser(@PathVariable id: Long): Nothing = TODO()

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long): Nothing = TODO()
}