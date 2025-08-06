package com.coursy.users.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.coursy.users.dto.RegistrationRequest
import com.coursy.users.dto.RoleUpdateRequest
import com.coursy.users.dto.UserResponse
import com.coursy.users.dto.toUserResponse
import com.coursy.users.failure.AuthorizationFailure
import com.coursy.users.failure.Failure
import com.coursy.users.failure.RoleFailure
import com.coursy.users.failure.UserFailure
import com.coursy.users.model.Role
import com.coursy.users.model.User
import com.coursy.users.repository.UserRepository
import com.coursy.users.repository.UserSpecification
import com.coursy.users.security.AuthorizationService
import getId
import jakarta.transaction.Transactional
import org.springframework.data.domain.PageRequest
import org.springframework.data.web.PagedResourcesAssembler
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.stereotype.Service
import readToken
import java.util.*
import kotlin.jvm.optionals.getOrElse

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authorizationService: AuthorizationService,
    private val pagedResourcesAssembler: PagedResourcesAssembler<UserResponse>
) {
    fun createUser(
        request: RegistrationRequest.Validated,
        tenantId: UUID?,
        jwt: PreAuthenticatedAuthenticationToken
    ): Either<Failure, Unit> {
        if (!authorizationService.canCreateUserWithRole(jwt, tenantId, request.roleName)) {
            return AuthorizationFailure.InsufficientRole.left()
        }

        val specification = UserSpecification
            .builder()
            .email(request.email)
            .tenantId(tenantId)
            .build()

        if (userRepository.exists(specification)) {
            return UserFailure.EmailAlreadyExists.left()
        }

        val user = createUserEntity(request, tenantId)
        userRepository.save(user)
        return Unit.right()
    }

    fun removeUser(
        id: UUID,
        jwt: PreAuthenticatedAuthenticationToken
    ): Either<Failure, Unit> {
        val user = userRepository
            .findById(id)
            .getOrElse { return UserFailure.IdNotExists.left() }

        if (!authorizationService.canRemoveUser(jwt, user)) {
            return AuthorizationFailure.InsufficientRole.left()
        }

        userRepository.removeUserById(id)
        return Unit.right()
    }

    fun getPrincipalUser(jwt: PreAuthenticatedAuthenticationToken): Either<Failure, UserResponse> {
        val id = jwt.getId()
        return userRepository.findById(id)
            .map { it.toUserResponse().right() }
            .getOrElse { UserFailure.IdNotExists.left() }
    }

    fun getUser(jwt: PreAuthenticatedAuthenticationToken, id: UUID): Either<Failure, UserResponse> {
        val user = userRepository
            .findById(id)
            .getOrElse { return UserFailure.IdNotExists.left() }
        if (!authorizationService.canFetchUser(jwt, user)) {
            return AuthorizationFailure.InsufficientRole.left()
        }
        return user.toUserResponse().right()
    }

    fun getUserPage(pageRequest: PageRequest) =
        userRepository.findAll(pageRequest)
            .map { it.toUserResponse() }
            .let { pagedResourcesAssembler.toModel(it) }

    fun updateUserRole(
        userId: UUID,
        request: RoleUpdateRequest.Validated,
        jwt: PreAuthenticatedAuthenticationToken
    ): Either<Failure, UserResponse> {
        val user = userRepository
            .findById(userId)
            .getOrElse { return UserFailure.IdNotExists.left() }

        if (!authorizationService.canUpdateUserRole(jwt, user, request.roleName)) {
            return AuthorizationFailure.InsufficientRole.left()
        }
        user.role = request.roleName
        return userRepository
            .save(user)
            .toUserResponse()
            .right()
    }

    private fun createUserEntity(request: RegistrationRequest.Validated, tenantId: UUID?): User {
        val encryptedPassword = passwordEncoder.encode(request.password.value)
        return User(
            email = request.email,
            password = encryptedPassword,
            role = request.roleName,
            firstName = request.firstName,
            lastName = request.lastName,
            tenantId = tenantId
        )
    }

    private fun canUpdateUserRole(
        principalRole: Role,
        newRole: Role,
        updatedUser: User
    ): Boolean {
        if (principalRole == Role.ROLE_ADMIN) {
            // Admin tries to assign SUPER_ADMIN
            if (newRole == Role.ROLE_SUPER_ADMIN) {
                return false
            }

            // Admin tries to change another ADMIN or SUPER_ADMIN
            if (updatedUser.role.name == Role.ROLE_SUPER_ADMIN) {
                return false
            }
            if (updatedUser.role.name == Role.ROLE_ADMIN) {
                return false
            }
        }
        return true
    }

    private fun canDeleteUser(
        principalRole: Role,
        user: User
    ): Boolean {
        if (principalRole == Role.ROLE_ADMIN) {
            // Admin tries to delete SUPER_ADMIN
            if (user.role.name == Role.ROLE_SUPER_ADMIN) {
                return false
            }

            // Admin tries to delete another ADMIN
            if (user.role.name == Role.ROLE_ADMIN) {
                return false
            }
        }
        return true
    }
}
