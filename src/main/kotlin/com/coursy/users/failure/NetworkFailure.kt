package com.coursy.users.failure

class NetworkFailure(
    val message: String
) : Failure {
    override fun message(): String = message
}
