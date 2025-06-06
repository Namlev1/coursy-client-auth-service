package com.coursy.clientauthservice.integration

import io.kotest.core.spec.style.BehaviorSpec
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerAuthenticationTest(
    private val mockMvc: MockMvc
) : BehaviorSpec() {
    val url = "/v1/users"

    init {
        given("user is not authenticated") {
            `when`("accessing /users") {
                val result = mockMvc.get(url)
                then("should return 403") {
                    result.andExpect { status { isForbidden() } }
                }
            }
        }
    }
}