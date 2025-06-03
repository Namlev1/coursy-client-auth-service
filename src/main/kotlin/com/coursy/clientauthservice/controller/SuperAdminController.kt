package com.coursy.clientauthservice.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/super-admin")
class SuperAdminController {

    @GetMapping
    fun authorizedEndpoint() = "You've passed the authorization flow!"
}