package com.coursy.users.service

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import com.coursy.users.dto.JwtResponse
import com.coursy.users.dto.LoginRequest
import com.coursy.users.dto.RefreshJwtRequest
import com.coursy.users.failure.AuthenticationFailure
import com.coursy.users.failure.Failure
import com.coursy.users.failure.RefreshTokenFailure
import com.coursy.users.jwt.JwtTokenService
import com.coursy.users.repository.UserRepository
import com.coursy.users.security.UserDetailsImp
import com.coursy.users.security.toUserDetails
import jakarta.transaction.Transactional
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.time.Instant

@Service
@Transactional
class AuthService(
    private val userRepository: UserRepository,
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenService: JwtTokenService,
    private val refreshTokenService: RefreshTokenService
) {
    fun authenticateUser(loginRequest: LoginRequest.Validated): Either<Failure, JwtResponse> {
        val authentication = runCatching {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(loginRequest.email.value, loginRequest.password.value)
            )
        }.getOrElse { return AuthenticationFailure.InvalidCredentials.left() }

        SecurityContextHolder.getContext().authentication = authentication
        val jwt = jwtTokenService.generateJwtToken(authentication)

        val userDetails = authentication.principal as UserDetailsImp

        val refreshToken = refreshTokenService.createRefreshToken(userDetails.id)
            .fold(
                { failure -> return failure.left() },
                { token -> token.token }
            )

        updateLastLogin(userDetails)

        return JwtResponse(
            token = jwt,
            refreshToken = refreshToken
        ).right()
    }

    fun refreshJwtToken(refreshRequest: RefreshJwtRequest.Validated): Either<RefreshTokenFailure, JwtResponse> {
        val refreshToken = refreshTokenService.findByToken(refreshRequest.refreshToken)
            .getOrElse { failure -> return failure.left() }

        refreshTokenService.verifyExpiration(refreshToken)
            .onLeft { failure -> return failure.left() }

        val userDetails = refreshToken.user.toUserDetails()
        val newJwt = jwtTokenService.generateJwtToken(userDetails)

        return JwtResponse(
            token = newJwt,
            refreshToken = refreshToken.token
        ).right()
    }

    private fun updateLastLogin(userDetails: UserDetailsImp) {
        val user = userRepository.findById(userDetails.id).get()
        user.lastLogin = Instant.now()
        user.failedAttempts = 0
        userRepository.save(user)
    }
}
