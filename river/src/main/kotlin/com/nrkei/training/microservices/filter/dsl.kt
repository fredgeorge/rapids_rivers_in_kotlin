/*
 * Copyright (c) 2022 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George  fredgeorge@acm.org
 */

package com.nrkei.training.microservices.filter

fun rules(block: ValidationsBuilder.() -> Unit) =
    ValidationsBuilder().also(block).results

class ValidationsBuilder internal constructor() {
    internal val results = mutableListOf<Validation>()

    val require get() = RequiredKeyBuilder()
    val forbid get() = ForbiddenKeyBuilder()

    inner class RequiredKeyBuilder() {
        private lateinit var key: String

        infix fun key(key: String) = this.also {
            results.add(KeyExistanceValidation(key))
            this.key = key
        }

        infix fun value(value: Any) {
            results.remove(results.last())
            results.add(KeyValueValidation(key, value))
        }
    }

    inner class ForbiddenKeyBuilder() {
        infix fun key(key: String) { results.add(KeyAbsenseValidation(key)) }
    }
}