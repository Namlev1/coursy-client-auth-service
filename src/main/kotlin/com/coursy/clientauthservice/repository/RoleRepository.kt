package com.coursy.clientauthservice.repository

import com.coursy.clientauthservice.model.Role
import com.coursy.clientauthservice.model.RoleName
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RoleRepository : JpaRepository<Role, Long> {
    fun existsByName(roleName: RoleName): Boolean
    fun findByName(roleName: RoleName): Optional<Role>
}