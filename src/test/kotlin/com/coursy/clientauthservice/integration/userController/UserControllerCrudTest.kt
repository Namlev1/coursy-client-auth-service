package com.coursy.clientauthservice.integration.userController

import com.coursy.clientauthservice.dto.RoleUpdateRequest
import com.coursy.clientauthservice.failure.AuthorizationFailure
import com.coursy.clientauthservice.model.RoleName
import com.coursy.clientauthservice.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.optional.shouldBeEmpty
import jakarta.transaction.Transactional
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.*

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerCrudTest(
    private val mockMvc: MockMvc,
    private val userRepository: UserRepository,
    private val mapper: ObjectMapper
) : BehaviorSpec() {

    val fixtures = UserTestFixtures()
    val authUrl = fixtures.authUrl
    val url = fixtures.userUrl

    init {
        given("a user in database") {
            `when`("retrieving user data") {
                then("should return 200") {
                    val userId = registerUser()

                    val result = mockMvc.get("$url/$userId") {
                        with(user("testuser").roles("ADMIN"))
                    }

                    result.andExpect {
                        status { isOk() }
                    }
                }

                then("should return registered user") {
                    val userId = registerUser()

                    val result = mockMvc.get("$url/$userId") {
                        with(user("testuser").roles("ADMIN"))
                    }

                    result.andExpect {
                        jsonPath("$.email") { value(fixtures.registrationEmail) }
                    }
                }
            }

            `when`("removing the user") {
                then("should return 204") {
                    val userId = registerUser()

                    val result = mockMvc.delete("$url/$userId") {
                        with(user("testuser").roles("ADMIN"))
                    }

                    result.andExpect {
                        status { isNoContent() }
                    }
                }

                then("user should not be in database") {
                    val userId = registerUser()

                    mockMvc.delete("$url/$userId") {
                        with(user("testuser").roles("ADMIN"))
                    }

                    userRepository.findById(userId).shouldBeEmpty()
                }
            }

            and("role updates scenarios") {
                `when`("ADMIN attempts to change user role") {
                    and("tries to promote to ADMIN") {
                        then("should return 200") {
                            val userId = registerUser()

                            val request = RoleUpdateRequest(RoleName.ROLE_ADMIN.name)
                            val result = mockMvc.put("$url/$userId") {
                                content = mapper.writeValueAsString(request)
                                contentType = MediaType.APPLICATION_JSON
                                with(user(fixtures.adminDetails))
                            }

                            result.andExpect {
                                status { isOk() }
                            }
                        }
                    }

                    and("tries to promote to SUPER_ADMIN") {
                        then("should return 403") {
                            val userId = registerUser()

                            val request = RoleUpdateRequest(RoleName.ROLE_SUPER_ADMIN.name)
                            val result = mockMvc.put("$url/$userId") {
                                content = mapper.writeValueAsString(request)
                                contentType = MediaType.APPLICATION_JSON
                                with(user(fixtures.adminDetails))
                            }

                            result.andExpect {
                                status { isForbidden() }
                            }
                        }

                        then("should return AuthorizationFailure") {
                            val userId = registerUser()

                            val request = RoleUpdateRequest(RoleName.ROLE_SUPER_ADMIN.name)
                            val result = mockMvc.put("$url/$userId") {
                                content = mapper.writeValueAsString(request)
                                contentType = MediaType.APPLICATION_JSON
                                with(user(fixtures.adminDetails))
                            }

                            result.andExpect {
                                jsonPath("$") { value(AuthorizationFailure.InsufficientRole.message()) }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun registerUser(): Long {
        val registrationRequest = fixtures.registrationRequest
        mockMvc
            .post("$authUrl/register") {
                content = mapper.writeValueAsString(registrationRequest)
                contentType = MediaType.APPLICATION_JSON
            }
            .andExpect { status { isCreated() } }
        val userId = userRepository.findByEmail(fixtures.registrationEmail)?.id
            ?: throw IllegalStateException("Error when registering user")
        return userId
    }
}