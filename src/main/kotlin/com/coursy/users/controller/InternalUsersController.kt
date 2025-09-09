package com.coursy.users.controller

import com.coursy.users.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RequestMapping("/internal/users")
@RestController
class InternalUsersController(
    private val userService: UserService,
    private val httpFailureResolver: HttpFailureResolver
) {

    @GetMapping("/{id}/roles")
    fun getUserRoles(@PathVariable id: UUID): ResponseEntity<Any> {
        return userService
            .getUserRole(id)
            .fold(
                { failure -> httpFailureResolver.handleFailure(failure) },
                { roles -> ResponseEntity.ok(roles) }
            )
    }
}