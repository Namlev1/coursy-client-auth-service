package com.coursy.clientauthservice.setup

import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("init")
class RoleSetup {
    @PostConstruct
    fun init() {
        println("role setup here")
    }
}