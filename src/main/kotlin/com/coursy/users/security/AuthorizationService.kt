package com.coursy.users.security

import arrow.core.getOrElse
import com.coursy.users.model.Role
import com.coursy.users.model.User
import com.coursy.users.model.toRole
import getPlatformId
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
        val principalPlatformId = jwt.getPlatformId()
        

        return when (principalRole) {
            Role.ROLE_HOST_OWNER, Role.ROLE_HOST_ADMIN -> true // Can create any admin role
            Role.ROLE_PLATFORM_OWNER, Role.ROLE_PLATFORM_ADMIN -> {
                // Can only create platform admins within their tenant
                when (targetRole) {
                    Role.ROLE_PLATFORM_ADMIN -> targetTenantId == principalPlatformId
                    Role.ROLE_PLATFORM_OWNER -> false // Cannot create other platform owners
                    else -> false
                }
            }
            else -> false
        }
    }

    fun canRemoveUser(jwt: PreAuthenticatedAuthenticationToken, user: User): Boolean {
        val principalRole = jwt
            .authorities
            .first()
            .toRole()
            .getOrElse {
                logger.warn("Invalid role in JWT: ${it.message}")
                return false
            }

        val principalPlatformId = jwt.getPlatformId()
        val targetUserPlatformId = user.platformId

        return when (principalRole) {
            Role.ROLE_HOST_OWNER -> true // Can do anything

            Role.ROLE_HOST_ADMIN -> {
                // Can remove TENANT and everything related with PLATFORM and STUDENT
                user.role in listOf(
                    Role.ROLE_TENANT,
                    Role.ROLE_PLATFORM_OWNER,
                    Role.ROLE_PLATFORM_ADMIN,
                    Role.ROLE_PLATFORM_USER
                )
            }

            Role.ROLE_PLATFORM_OWNER -> {
                // Can do anything within their platform (same tenant)
                targetUserPlatformId == principalPlatformId
            }

            Role.ROLE_PLATFORM_ADMIN -> {
                // Can remove only students within their platform
                targetUserPlatformId == principalPlatformId &&
                        user.role == Role.ROLE_PLATFORM_USER
            }

            Role.ROLE_TENANT, Role.ROLE_PLATFORM_USER -> false // Can't remove anyone
        }
    }
    fun canFetchUser(jwt: PreAuthenticatedAuthenticationToken, user: User): Boolean {
        val principalRole = jwt
            .authorities
            .first()
            .toRole()
            .getOrElse {
                logger.warn("Invalid role in JWT: ${it.message}")
                return false
            }

        val principalPlatformId = jwt.getPlatformId()
        val targetUserPlatformId = user.platformId

        return when (principalRole) {
            Role.ROLE_HOST_OWNER -> true // Can do anything

            Role.ROLE_HOST_ADMIN -> true // Can fetch every user, even owner

            Role.ROLE_TENANT -> false // Tenants can't fetch

            Role.ROLE_PLATFORM_OWNER -> {
                // Can do anything within platform
                targetUserPlatformId == principalPlatformId
            }

            Role.ROLE_PLATFORM_ADMIN -> {
                // Can fetch every account within their platform
                targetUserPlatformId == principalPlatformId
            }

            Role.ROLE_PLATFORM_USER -> {
                // Can only fetch other users (not admins and owner) within their platform
                targetUserPlatformId == principalPlatformId &&
                        user.role == Role.ROLE_PLATFORM_USER
            }
        }
    }
    fun canUpdateUserRole(jwt: PreAuthenticatedAuthenticationToken, targetUser: User, newRole: Role): Boolean {
        val principalRole = jwt
            .authorities
            .first()
            .toRole()
            .getOrElse {
                logger.warn("Invalid role in JWT: ${it.message}")
                return false
            }

        val principalPlatformId = jwt.getPlatformId()
        val targetUserPlatformId = targetUser.platformId

        return when (principalRole) {
            Role.ROLE_HOST_OWNER -> true

            Role.ROLE_HOST_ADMIN -> {
                // Can assign admin roles (any admin role)
                newRole in listOf(
                    Role.ROLE_HOST_ADMIN,
                    Role.ROLE_PLATFORM_OWNER,
                    Role.ROLE_PLATFORM_ADMIN
                )
            }

            Role.ROLE_PLATFORM_OWNER -> {
                // Can do anything within their platform
                targetUserPlatformId == principalPlatformId
            }

            Role.ROLE_PLATFORM_ADMIN -> {
                // Can assign admin roles within their platform
                targetUserPlatformId == principalPlatformId &&
                        newRole in listOf(
                    Role.ROLE_PLATFORM_ADMIN,
                    Role.ROLE_PLATFORM_USER
                )
            }

            Role.ROLE_TENANT, Role.ROLE_PLATFORM_USER -> false // Can't update anything
        }
    }
}