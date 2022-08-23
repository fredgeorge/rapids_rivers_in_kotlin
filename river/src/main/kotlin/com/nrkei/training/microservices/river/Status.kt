/*
 * Copyright (c) 2022 by Fred George
 * @author Fred George  fredgeorge@acm.org
 * Licensed under the MIT License; see LICENSE file in root.
 */

package com.nrkei.training.microservices.river

// Understands issues that arose when analyzing a JSON message
// Implements Collecting Parameter in Refactoring by Martin Fowler
class Status(private val jsonString: String) {
    private val informationalMessages = mutableListOf<String>()
    private val warnings = mutableListOf<String>()
    private val errors = mutableListOf<String>()
    private val severeErrors = mutableListOf<String>()

    fun hasErrors() = errors.isNotEmpty() || severeErrors.isNotEmpty()

    fun hasMessages() = hasErrors() || warnings.isNotEmpty() || informationalMessages.isNotEmpty()

    internal fun informationOnly(explanation: String) = informationalMessages.add(explanation)

    internal fun warning(explanation: String) = warnings.add(explanation)

    internal fun error(explanation: String) = errors.add(explanation)

    internal fun severeError(explanation: String) = severeErrors.add(explanation)
        .also { throw PacketProblemsException(this) }

    override fun toString(): String {
        if (!hasMessages()) return "No errors detected in JSON:\n\t$jsonString"
        val results = StringBuffer()
        results.append("Errors and/or messages exist. Original JSON string is:\n\t")
        results.append(jsonString)
        append("Severe errors", severeErrors, results)
        append("Errors", errors, results)
        append("Warnings", warnings, results)
        append("Information", informationalMessages, results)
        results.append("\n")
        return results.toString()
    }

    private fun append(label: String, messages: List<String>, results: StringBuffer) {
        if (messages.isEmpty()) return
        results.append("\n")
        results.append(label)
        results.append(": ")
        results.append(messages.size)
        for (message in messages) {
            results.append("\n\t")
            results.append(message)
        }
    }

    inner class PacketProblemsException internal constructor(problems: Status):RuntimeException() {
        override val message = problems.toString()
    }
}