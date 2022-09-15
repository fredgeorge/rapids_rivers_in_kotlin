/*
 * Copyright (c) 2022 by Fred George
 * @author Fred George  fredgeorge@acm.org
 * Licensed under the MIT License; see LICENSE file in root.
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
            results.add(KeyExistenceValidation(key))
            this.key = key
        }

        infix fun value(value: Any) {
            results.remove(results.last())
            results.add(KeyValueValidation(key, value))
        }
    }

    inner class ForbiddenKeyBuilder() {
        infix fun key(key: String) { results.add(KeyAbsenceValidation(key)) }
    }
}