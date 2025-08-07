package com.coursy.users.service

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import com.coursy.users.dto.RegistrationRequest
import com.coursy.users.dto.RoleUpdateRequest
import com.coursy.users.dto.UserResponse
import com.coursy.users.dto.toUserResponse
import com.coursy.users.failure.AuthorizationFailure
import com.coursy.users.failure.Failure
import com.coursy.users.failure.UserFailure
import com.coursy.users.model.User
import com.coursy.users.repository.UserRepository
import com.coursy.users.repository.UserSpecification
import com.coursy.users.security.AuthorizationService
import getId
import jakarta.transaction.Transactional
import org.springframework.data.domain.PageRequest
import org.springframework.data.web.PagedResourcesAssembler
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.PagedModel
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.stereotype.Service
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
    fun createHostUser(
        request: RegistrationRequest.Validated,
        jwt: PreAuthenticatedAuthenticationToken
    ) = createUser(request, null, jwt)

    fun createPlatformUser(
        request: RegistrationRequest.Validated,
        platformId: UUID?,
        jwt: PreAuthenticatedAuthenticationToken
    ) = createUser(request, platformId, jwt)

    private fun createUser(
        request: RegistrationRequest.Validated,
        platformId: UUID?,
        jwt: PreAuthenticatedAuthenticationToken
    ): Either<Failure, Unit> {
        if (!authorizationService.canCreateUserWithRole(jwt, platformId, request.roleName)) {
            return AuthorizationFailure.InsufficientRole.left()
        }

        val specification = UserSpecification
            .builder()
            .email(request.email)
            .platformId(platformId)
            .build()

        if (userRepository.exists(specification)) {
            return UserFailure.EmailAlreadyExists.left()
        }

        val user = createUserEntity(request, platformId)
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

    fun getUserPage(
        jwt: PreAuthenticatedAuthenticationToken,
        pageRequest: PageRequest
    ): Either<Failure, PagedModel<EntityModel<UserResponse>>> {
        val specification = authorizationService
            .getUserFetchSpecification(jwt)
            .getOrElse { failure -> return failure.left() }
        return userRepository.findAll(specification, pageRequest)
            .map { it.toUserResponse() }
            .let { pagedResourcesAssembler.toModel(it) }
            .right()
    }

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

    private fun createUserEntity(request: RegistrationRequest.Validated, platformId: UUID?): User {
        val encryptedPassword = passwordEncoder.encode(request.password.value)
        return User(
            email = request.email,
            password = encryptedPassword,
            role = request.roleName,
            firstName = request.firstName,
            lastName = request.lastName,
            platformId = platformId
        )
    }
}
