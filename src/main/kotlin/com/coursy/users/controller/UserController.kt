package com.coursy.users.controller

import arrow.core.flatMap
import com.coursy.users.dto.ChangePasswordRequest
import com.coursy.users.dto.RoleUpdateRequest
import com.coursy.users.model.RoleName
import com.coursy.users.security.UserDetailsImp
import com.coursy.users.service.UserService
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RequestMapping("/users")
@RestController
class UserController(
    private val userService: UserService,
    private val httpFailureResolver: HttpFailureResolver
) {
    @GetMapping("/me")
    fun getCurrentUser(@AuthenticationPrincipal currentUser: UserDetailsImp): ResponseEntity<Any> {
        return userService
            .getUser(currentUser.id)
            .fold(
                { failure -> httpFailureResolver.handleFailure(failure) },
                { response -> ResponseEntity.status(HttpStatus.OK).body(response) }
            )
    }

    @PutMapping("/me/password")
    fun updateCurrentUserPassword(
        @AuthenticationPrincipal currentUser: UserDetailsImp,
        @RequestBody request: ChangePasswordRequest
    ): ResponseEntity<Any> {
        val result = request
            .validate()
            .flatMap { validated ->
                userService.updatePassword(currentUser.id, validated)
            }

        return result.fold(
            { failure -> httpFailureResolver.handleFailure(failure) },
            { ResponseEntity.status(HttpStatus.OK).build() }
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