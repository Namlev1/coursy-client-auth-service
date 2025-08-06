package com.coursy.users.repository

import com.coursy.users.model.User
import com.coursy.users.types.Email
import com.coursy.users.types.Name
import org.springframework.data.jpa.domain.Specification
import java.util.*

class UserSpecification {
    companion object {
        fun builder() = Builder()
    }

    class Builder {
        private val predicates = mutableListOf<Specification<User>>()

        fun id(id: UUID?) = apply {
            id?.let {
                predicates.add { root, _, cb ->
                    cb.equal(root.get<UUID>("id"), it)
                }
            }
        }

        fun tenantId(tenantId: UUID?) = apply {
            predicates.add { root, _, cb ->
                if (tenantId == null) {
                    cb.isNull(root.get<UUID>("tenantId"))
                } else {
                    cb.equal(root.get<UUID>("tenantId"), tenantId)
                }
            }
        }
        
        fun email(email: Email?) = apply {
            email?.let {
                predicates.add { root, _, cb ->
                    cb.equal(root.get<Email>("email"), it)
                }
            }
        }

        fun firstName(firstName: String?) = apply {
            firstName?.let {
                predicates.add { root, _, cb ->
                    cb.equal(root.get<Name>("firstName"), it)
                }
            }
        }

        fun lastName(lastName: String?) = apply {
            lastName?.let {
                predicates.add { root, _, cb ->
                    cb.equal(root.get<Name>("firstName"), it)
                }
            }
        }

        fun build(): Specification<User> {
            return predicates.reduce { acc, spec -> acc.and(spec) }
        }
    }
}
