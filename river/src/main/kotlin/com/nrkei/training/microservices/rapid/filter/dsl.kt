package com.nrkei.training.microservices.rapid.filter

fun rules(block: ValidationsBuilder.() -> Unit) =
    ValidationsBuilder().also(block).results

class ValidationsBuilder internal constructor() {
    internal val results = mutableListOf<Validation>()
    val require get() = RequiredKeyBuilder()
    val forbid get() = ForbiddenKeyBuilder()


    inner class RequiredKeyBuilder() {
        infix fun key(key: String) = this
        infix fun value(value: String) {}
    }

    inner class ForbiddenKeyBuilder() {
        infix fun key(key: String) {}
    }
}