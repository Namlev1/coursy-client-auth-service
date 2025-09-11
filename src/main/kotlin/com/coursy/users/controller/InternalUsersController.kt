package com.coursy.users.controller

import com.coursy.users.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RequestMapping("/api/internal/users")
@RestController
class InternalUsersController(
    private val userService: UserService,
    private val httpFailureResolver: HttpFailureResolver
) {

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: UUID) = userService
        .getUser(id)
        .fold(
            { failure -> httpFailureResolver.handleFailure(failure) },
            { response -> ResponseEntity.status(HttpStatus.OK).body(response) }
        )
}