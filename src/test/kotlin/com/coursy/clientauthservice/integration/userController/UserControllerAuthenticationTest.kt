package com.coursy.clientauthservice.integration.userController

import io.kotest.core.spec.style.BehaviorSpec
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerAuthenticationTest(
    private val mockMvc: MockMvc
) : BehaviorSpec() {
    val fixtures = UserTestFixtures()
    val url = fixtures.userUrl

    init {
        given("user is not authenticated") {
            `when`("accessing /users") {
                val result = mockMvc.get(url)
                then("should return 403") {
                    result.andExpect { status { isForbidden() } }
                }
            }
        }
        given("user is STUDENT") {
            `when`("accessing /users") {
                val result = mockMvc.get(url) {
                    with(user(fixtures.studentDetails))
                }
                then("should return 403") {
                    result.andExpect { status { isForbidden() } }
                }
            }
        }
        given("user is ADMIN") {
            `when`("accessing /users") {
                val result = mockMvc.get(url) {
                    with(user(fixtures.adminDetails))
                }
                then("should return 200") {
                    result.andExpect { status { isOk() } }
                }
            }
        }
        given("user is SUPER_ADMIN") {
            `when`("accessing /users") {
                val result = mockMvc.get(url) {
                    with(user(fixtures.superAdmirDetails))
                }
                then("should return 200") {
                    result.andExpect { status { isOk() } }
                }
            }
        }
    }
}