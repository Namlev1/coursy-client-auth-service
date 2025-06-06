package com.coursy.clientauthservice.integration.userController

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.BehaviorSpec
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerCrudTest(
    private val mockMvc: MockMvc,
    private val mapper: ObjectMapper
) : BehaviorSpec() {
    val fixtures = UserTestFixtures()
    val authUrl = fixtures.authUrl
    val url = fixtures.userUrl

    init {
        given("a user in database") {
            val registrationRequest = fixtures.registrationRequest
            mockMvc
                .post("$authUrl/register") {
                    content = mapper.writeValueAsString(registrationRequest)
                    contentType = MediaType.APPLICATION_JSON
                }
                .andExpect { status { isCreated() } }
            val userId = 1

            `when`("retrieving user data") {
                val result = mockMvc.get("$url/$userId") {
                    with(user("testuser").roles("ADMIN"))
                }

                then("should find the user") {
                    result.andExpect { status { isOk() } }
                }

            }
        }
    }
}