package com.coursy.clientauthservice.controller

import arrow.core.flatMap
import com.coursy.clientauthservice.dto.LoginRequest
import com.coursy.clientauthservice.service.AuthService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
//    private val httpFailureResolver: HttpFailureResolver
) {
    @PostMapping("/login")
    fun authenticateUser(@RequestBody request: LoginRequest): ResponseEntity<Any> {
        val result = request.validate().flatMap { validated -> authService.authenticateUser(validated) }

        return result.fold(
            { failure -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(failure.message()) },
            { jwtResponse -> ResponseEntity.status(HttpStatus.OK).body(jwtResponse) }
        )
    }

//    @GetMapping("/refresh")
//    fun refreshJwt(@RequestBody request: RefreshJwtRequest): ResponseEntity<Any> {
//        val result = request.validate().flatMap { validated -> authService.refreshJwtToken(validated) }
//
//        return result.fold(
//            { failure -> httpFailureResolver.handleFailure(failure) },
//            { jwtResponse -> ResponseEntity.status(HttpStatus.OK).body(jwtResponse) }
//        )
//    }

}
