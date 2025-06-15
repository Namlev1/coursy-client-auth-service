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
import com.coursy.users.model.RoleName
import com.coursy.users.model.User
import com.coursy.users.repository.RoleRepository
import com.coursy.users.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.PageRequest
import org.springframework.data.web.PagedResourcesAssembler
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrElse

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
    private val pagedResourcesAssembler: PagedResourcesAssembler<UserResponse>
) {
    fun createUser(request: RegistrationRequest.Validated): Either<Failure, Unit> {
        if (userRepository.existsByEmail(request.email)) {
            return UserFailure.EmailAlreadyExists.left()
        }

        if (userRepository.existsByLogin(request.login)) {
            return UserFailure.LoginAlreadyExists.left()
        }

        val role = roleRepository.findByName(request.roleName)
            .getOrElse { return RoleFailure.NotFound.left() }

        val user = createUser(request, role)
        userRepository.save(user)
        return Unit.right()
    }

    fun removeUser(
        id: Long,
        principalRole: RoleName
    ): Either<Failure, Unit> {
        val user = userRepository
            .findById(id)
            .getOrElse { return UserFailure.IdNotExists.left() }

        if (!canDeleteUser(principalRole, user)) {
            return AuthorizationFailure.InsufficientRole.left()
        }

        userRepository.removeUserById(id)
        return Unit.right()
    }

    fun getUser(id: Long): Either<Failure, UserResponse> {
        return userRepository.findById(id)
            .map { it.toUserResponse().right() }
            .getOrElse { UserFailure.IdNotExists.left() }
    }

    fun getUserPage(pageRequest: PageRequest) =
        userRepository.findAll(pageRequest)
            .map { it.toUserResponse() }
            .let { pagedResourcesAssembler.toModel(it) }

    fun updateUserRole(
        userId: Long,
        request: RoleUpdateRequest.Validated,
        principalRole: RoleName
    ): Either<Failure, UserResponse> {
        val user = userRepository
            .findById(userId)
            .getOrElse { return UserFailure.IdNotExists.left() }

        if (!canUpdateUserRole(principalRole, request.roleName, user)) {
            return AuthorizationFailure.InsufficientRole.left()
        }

        val role = roleRepository
            .findByName(request.roleName)
            .getOrElse { return RoleFailure.NotFound.left() }

        user.role = role
        return userRepository
            .save(user)
            .toUserResponse().right()
    }

    private fun createUser(request: RegistrationRequest.Validated, role: Role): User {
        val encryptedPassword = passwordEncoder.encode(request.password.value)
        return User(
            login = request.login,
            email = request.email,
            password = encryptedPassword,
            role = role
        )
    }

    private fun canUpdateUserRole(
        principalRole: RoleName,
        newRole: RoleName,
        updatedUser: User
    ): Boolean {
        if (principalRole == RoleName.ROLE_ADMIN) {
            // Admin tries to assign SUPER_ADMIN
            if (newRole == RoleName.ROLE_SUPER_ADMIN) {
                return false
            }

            // Admin tries to change another ADMIN or SUPER_ADMIN
            if (updatedUser.role.name == RoleName.ROLE_SUPER_ADMIN) {
                return false
            }
            if (updatedUser.role.name == RoleName.ROLE_ADMIN) {
                return false
            }
        }
        return true
    }

    private fun canDeleteUser(
        principalRole: RoleName,
        user: User
    ): Boolean {
        if (principalRole == RoleName.ROLE_ADMIN) {
            // Admin tries to delete SUPER_ADMIN
            if (user.role.name == RoleName.ROLE_SUPER_ADMIN) {
                return false
            }

            // Admin tries to delete another ADMIN
            if (user.role.name == RoleName.ROLE_ADMIN) {
                return false
            }
        }
        return true
    }
}
