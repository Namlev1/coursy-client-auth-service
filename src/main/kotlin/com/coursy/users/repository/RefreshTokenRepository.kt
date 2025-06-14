package com.coursy.users.repository

import com.coursy.users.model.RefreshToken
import com.coursy.users.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    fun deleteByUser(user: User)
    fun findByToken(token: String): RefreshToken?
} 
