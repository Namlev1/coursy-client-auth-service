package com.coursy.clientauthservice.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.coursy.clientauthservice.dto.*
import com.coursy.clientauthservice.failure.Failure
import com.coursy.clientauthservice.failure.RoleFailure
import com.coursy.clientauthservice.failure.UserFailure
import com.coursy.clientauthservice.model.Role
import com.coursy.clientauthservice.model.User
import com.coursy.clientauthservice.repository.RoleRepository
import com.coursy.clientauthservice.repository.UserRepository
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

    fun removeUser(id: Long): Either<Failure, Unit> {
        userRepository
            .findById(id)
            .getOrElse { return UserFailure.IdNotExists.left() }

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
    ): Either<Failure, UserResponse> {
        val user = userRepository
            .findById(userId)
            .getOrElse { return UserFailure.IdNotExists.left() }

        val role = roleRepository
            .findByName(request.roleName)
            .getOrElse { return RoleFailure.NotFound.left() }
       
        user.role = role
        return userRepository
            .save(user)
            .toUserResponse().right()
    }

    fun updatePassword(
        userId: Long,
        request: ChangePasswordRequest.Validated
    ): Either<Failure, Unit> {
        val user = userRepository
            .findById(userId)
            .getOrElse { return UserFailure.IdNotExists.left() }

        user.password = passwordEncoder.encode(request.password.value)
        userRepository.save(user)
        return Unit.right()
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

}
