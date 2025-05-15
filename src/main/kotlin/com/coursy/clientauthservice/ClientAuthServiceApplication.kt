package com.coursy.clientauthservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ClientAuthServiceApplication

fun main(args: Array<String>) {
    runApplication<ClientAuthServiceApplication>(*args)
}
