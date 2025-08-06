package com.coursy.users.security

import arrow.core.getOrElse
import com.coursy.users.model.Role
import com.coursy.users.model.toRole
import getTenantId
import org.slf4j.LoggerFactory
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.stereotype.Service
import java.util.*

@Service
class AuthorizationService {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun canCreateUserWithRole(jwt: PreAuthenticatedAuthenticationToken?, targetTenantId: UUID?, targetRole: Role): Boolean {
        // No authentication required for basic account creation
        if (targetRole in listOf(Role.ROLE_TENANT, Role.ROLE_PLATFORM_USER)) {
            return true
        }

        // Administrative roles require authentication
        if (jwt == null) {
            return false
        }
        
        val principalRole = jwt
            .authorities
            .first()
            .toRole()
            .getOrElse {
            logger.warn("Invalid role in JWT: ${it.message}")
            return false
        }
        val principalTenantId = jwt.getTenantId()
        

        return when (principalRole) {
            Role.ROLE_HOST_OWNER, Role.ROLE_HOST_ADMIN -> true // Can create any admin role
            Role.ROLE_PLATFORM_OWNER, Role.ROLE_PLATFORM_ADMIN -> {
                // Can only create platform admins within their tenant
                when (targetRole) {
                    Role.ROLE_PLATFORM_ADMIN -> targetTenantId == principalTenantId
                    Role.ROLE_PLATFORM_OWNER -> false // Cannot create other platform owners
                    else -> false
                }
            }
            else -> false
        }
    }

}