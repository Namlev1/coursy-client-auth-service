package com.coursy.clientauthservice.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.coursy.clientauthservice.security.UserDetailsImp
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.stereotype.Service
import java.util.*

@Service
class JwtTokenService(
    @Value("\${jwt.secret}")
    private var jwtSecret: String,

    @Value("\${jwt.access-token-expiration}")
    private var jwtExpirationMs: Int = 0
) {

    fun generateJwtToken(authentication: Authentication): String =
        (authentication.principal as UserDetailsImp).let { generateJwt(it.email.value, it.authorities) }

    fun generateJwtToken(userDetailsImp: UserDetailsImp): String =
        generateJwt(userDetailsImp.email.value, userDetailsImp.authorities)

    fun getUserEmailFromJwtToken(token: String): String =
        JWT.require(Algorithm.HMAC256(jwtSecret))
            .build()
            .verify(token)
            .subject

    fun isJwtTokenValid(authToken: String): Boolean =
        runCatching {
            JWT.require(Algorithm.HMAC256(jwtSecret)).build().verify(authToken)
            true
        }.getOrElse { false }

    private fun generateJwt(subject: String, roles: Collection<GrantedAuthority>): String = JWT.create()
        .withSubject(subject)
        .withClaim("roles", roles.map { it.authority })
        .withIssuedAt(Date())
        .withExpiresAt(Date(System.currentTimeMillis() + jwtExpirationMs))
        .sign(Algorithm.HMAC256(jwtSecret))
}