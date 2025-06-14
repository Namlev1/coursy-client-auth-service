package com.coursy.users.dto

import arrow.core.Either

interface SelfValidating<A, B> {
    fun validate(): Either<A, B>
}
