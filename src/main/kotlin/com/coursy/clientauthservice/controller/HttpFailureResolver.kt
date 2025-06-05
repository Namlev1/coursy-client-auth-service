package com.coursy.clientauthservice.controller

import com.coursy.clientauthservice.failure.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class HttpFailureResolver {
    fun handleFailure(failure: Failure): ResponseEntity<Any> =
        when (failure) {
            // Validation
            is EmailFailure -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(failure.message())
            is NameFailure -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(failure.message())
            is PasswordFailure -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(failure.message())
            is LoginTypeFailure -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(failure.message())
            is RoleFailure.NotFound -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(failure.message())
            is RoleFailure.IsNull -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(failure.message())

            // Refresh Token
            is RefreshTokenFailure.Empty -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(failure.message())
            is RefreshTokenFailure.NotFound -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(failure.message())
            is RefreshTokenFailure.Expired -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(failure.message())

            // Authentication
            is AuthenticationFailure -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(failure.message())
            is AuthHeaderFailure -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(failure.message())
            is JwtFailure -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(failure.message())

            // Authorization
            is AuthorizationFailure -> ResponseEntity.status(HttpStatus.FORBIDDEN).body(failure.message())

            // User creation
            is UserFailure.EmailAlreadyExists -> ResponseEntity.status(HttpStatus.CONFLICT).body(failure.message())
            is UserFailure.LoginAlreadyExists -> ResponseEntity.status(HttpStatus.CONFLICT).body(failure.message())
            is UserFailure.IdNotExists -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(failure.message())

            else -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(failure.message())
        }
}
