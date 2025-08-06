package com.coursy.users.setup

import com.coursy.users.model.Role
import com.coursy.users.model.Role
import com.coursy.users.repository.RoleRepository
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("setup")
class RoleSetup(
    @Value("\${app.setup.roles:USER,ADMIN,INSTRUCTOR}")
    private val rolesToSetup: String,
    private val repository: RoleRepository
) {
    private val logger = LoggerFactory.getLogger(RoleSetup::class.java)

    @PostConstruct
    fun setup() {
        logger.debug("Roles from config: $rolesToSetup")

        val roleNames = rolesToSetup.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { "ROLE_$it" }

        roleNames.forEach { roleName ->
            try {
                val roleEnum = Role.valueOf(roleName)
                if (repository.existsByName(roleEnum)) {
                    logger.warn("Role name $roleName exists in DB - skipping")
                    return@forEach
                }
                val role = Role(id = 0L, name = roleEnum)
                repository.save(role)
                logger.info("Created role: $roleName")
            } catch (e: IllegalArgumentException) {
                logger.error("Invalid role name: $roleName - skipping")
            }
        }

        logger.info("Role setup completed")
    }
}