package com.coursy.clientauthservice

import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment

@SpringBootApplication
class ClientAuthServiceApplication(
    private val environment: Environment
) {
    private val logger = LoggerFactory.getLogger(ClientAuthServiceApplication::class.java)

    @Profile("setup")
    @PreDestroy
    fun destroy() {
        if (environment.activeProfiles.contains("setup")) {
            logger.info("Application setup complete")
        }
    }
}

fun main(args: Array<String>) {
    runApplication<ClientAuthServiceApplication>(*args)
}