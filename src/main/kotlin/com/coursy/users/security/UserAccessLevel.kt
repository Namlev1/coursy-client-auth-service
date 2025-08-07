package com.coursy.users.security

import com.coursy.users.model.Role
import com.coursy.users.model.User
import com.coursy.users.repository.UserSpecification
import org.springframework.data.jpa.domain.Specification
import java.util.*

sealed class UserAccessLevel {
    abstract fun canAccess(user: User): Boolean
    abstract fun toSpecification(): Specification<User>

    object All : UserAccessLevel() {
        override fun canAccess(user: User): Boolean = true
        override fun toSpecification(): Specification<User> =
            UserSpecification.builder().build()
    }

    data class PlatformAll(val platformId: UUID?) : UserAccessLevel() {
        override fun canAccess(user: User): Boolean = user.platformId == platformId
        override fun toSpecification(): Specification<User> =
            UserSpecification.builder().platformId(platformId).build()
    }

    data class PlatformFiltered(
        val platformId: UUID?,
        val allowedRoles: List<Role>
    ) : UserAccessLevel() {
        override fun canAccess(user: User): Boolean =
            user.platformId == platformId && user.role in allowedRoles

        override fun toSpecification(): Specification<User> =
            UserSpecification.builder()
                .platformId(platformId)
                .roleIn(allowedRoles)
                .build()
    }
}