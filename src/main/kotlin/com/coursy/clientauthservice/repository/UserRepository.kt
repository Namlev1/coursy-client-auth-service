package com.coursy.clientauthservice.repository

import com.coursy.clientauthservice.model.User
import com.coursy.clientauthservice.types.Email
import com.coursy.clientauthservice.types.Login
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
    fun existsByEmail(email: Email): Boolean
    fun existsByLogin(login: Login): Boolean
    fun removeUserById(id: Long)
}
