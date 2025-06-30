package com.coursy.users.controller

import arrow.core.flatMap
import arrow.core.left
import com.coursy.users.dto.RegistrationRequest
import com.coursy.users.dto.RoleUpdateRequest
import com.coursy.users.failure.AuthorizationFailure
import com.coursy.users.model.RoleName
import com.coursy.users.security.UserDetailsImp
import com.coursy.users.service.UserService
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RequestMapping("/api/users")
@RestController
class UserController(
    private val userService: UserService,
    private val httpFailureResolver: HttpFailureResolver
) {

    @PostMapping("/register")
    fun createUser(@RequestBody request: RegistrationRequest): ResponseEntity<Any> {
        val result = request
            .validate()
            .flatMap { validated ->
                if (isRegistrationRolePermitted(validated))
                    userService.createUser(validated)
                else
                    AuthorizationFailure.InsufficientRole.left()
            }

        return result.fold(
            { failure -> httpFailureResolver.handleFailure(failure) },
            { ResponseEntity.status(HttpStatus.CREATED).build() }
        )
    }

    @PostMapping("/register/admin")
    fun createAdmin(@RequestBody request: RegistrationRequest): ResponseEntity<Any> {
        val result = request
            .copy(roleName = RoleName.ROLE_ADMIN.toString())
            .validate()

        return result.fold(
            { failure -> httpFailureResolver.handleFailure(failure) },
            { ResponseEntity.status(HttpStatus.CREATED).build() }
        )
    }

    private fun isRegistrationRolePermitted(request: RegistrationRequest.Validated) =
        request.roleName == RoleName.ROLE_STUDENT || request.roleName == RoleName.ROLE_TEACHER
    @GetMapping("/me")
    fun getCurrentUser(@AuthenticationPrincipal currentUser: UserDetailsImp): ResponseEntity<Any> {
        return userService
            .getUser(currentUser.id)
            .fold(
                { failure -> httpFailureResolver.handleFailure(failure) },
                { response -> ResponseEntity.status(HttpStatus.OK).body(response) }
            )
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping
    fun getUserList(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ): ResponseEntity<Any> =
        when {
            arePageParamsInvalid(page, size) -> ResponseEntity.badRequest().build()
            else -> PageRequest.of(page, size)
                .let { userService.getUserPage(it) }
                .let { ResponseEntity.ok(it) }
        }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long) = userService
        .getUser(id)
        .fold(
            { failure -> httpFailureResolver.handleFailure(failure) },
            { response -> ResponseEntity.status(HttpStatus.OK).body(response) }
        )

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PutMapping("/{id}")
    fun updateUserRole(
        @PathVariable id: Long,
        @RequestBody request: RoleUpdateRequest,
        @AuthenticationPrincipal principal: UserDetailsImp
    ): ResponseEntity<Any> {
        val principalRole = extractPrincipalRole(principal)
        val result = request
            .validate()
            .flatMap { validated ->
                userService.updateUserRole(id, validated, principalRole)
            }

        return result.fold(
            { failure -> httpFailureResolver.handleFailure(failure) },
            { response -> ResponseEntity.status(HttpStatus.OK).body(response) }
        )
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    fun deleteUser(
        @PathVariable id: Long,
        @AuthenticationPrincipal principal: UserDetailsImp
    ): ResponseEntity<Any> {
        val principalRole = extractPrincipalRole(principal)

        return userService.removeUser(id, principalRole)
            .fold(
                { failure -> httpFailureResolver.handleFailure(failure) },
                { ResponseEntity.status(HttpStatus.NO_CONTENT).build() }
            )
    }

    private fun extractPrincipalRole(principal: UserDetailsImp): RoleName {
        val principalRole = RoleName.valueOf(principal.authorities.first().authority)
        return principalRole
    }

    private fun arePageParamsInvalid(page: Int, size: Int) =
        page < 0 || size <= 0
}