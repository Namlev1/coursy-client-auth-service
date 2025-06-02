package com.coursy.clientauthservice.controller

import arrow.core.flatMap
import com.coursy.clientauthservice.dto.RegistrationRequest
import com.coursy.clientauthservice.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/user")
@RestController
class UserController(
    private val userService: UserService,
) {

    @PostMapping
    fun createRegularUser(@RequestBody request: RegistrationRequest): ResponseEntity<Any> {
        val result = request
            .validate()
            .flatMap { validated -> userService.createUser(validated) }

        return result.fold(
            { failure -> ResponseEntity.status(HttpStatus.BAD_REQUEST).build() },
            { ResponseEntity.status(HttpStatus.CREATED).build() }
        )
    }
}