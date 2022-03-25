/*
 * Copyright (c) 2022 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George  fredgeorge@acm.org
 */

package com.nrkei.training.microservices.rapid.river

// Understands issues that arose when analyzing a JSON message
// Implements Collecting Parameter in Refactoring by Martin Fowler
class PacketProblems(private val jsonString: String) {
    private val informationalMessages = mutableListOf<String>()
    private val warnings = mutableListOf<String>()
    private val errors = mutableListOf<String>()
    private val severeErrors = mutableListOf<String>()

    fun hasErrors() = errors.isNotEmpty() || severeErrors.isNotEmpty()

    fun hasMessages() = hasErrors() || warnings.isNotEmpty() || informationalMessages.isNotEmpty()

    fun informationOnly(explanation: String) = informationalMessages.add(explanation)

    fun warning(explanation: String) = warnings.add(explanation)

    fun error(explanation: String) = errors.add(explanation)

    fun severeError(explanation: String) = severeErrors.add(explanation).also {
        throw PacketProblemsException(this)
    }

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

    inner class PacketProblemsException internal constructor(problems: PacketProblems):RuntimeException() {
        override val message = problems.toString()
    }
}