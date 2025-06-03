package com.coursy.clientauthservice.repository

import com.coursy.clientauthservice.model.RefreshToken
import com.coursy.clientauthservice.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    fun deleteByUser(user: User)
    fun findByToken(token: String): RefreshToken?
} 
