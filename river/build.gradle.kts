/*
 * Copyright (c) 2022 by Fred George
 * @author Fred George  fredgeorge@acm.org
 * Licensed under the MIT License; see LICENSE file in root.
 */
 
plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    kotlin("jvm")

    // Apply the java-library plugin for API and implementation separation.
    `java-library`
}

repositories {
    mavenLocal()
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.8.0"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.0")

    // Jupiter using JUnit 5
    testImplementation(platform("org.junit:junit-bom:5.9.2"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")

    // Need Jackson for JSON support
    implementation("com.fasterxml.jackson.module", "jackson-module-kotlin", "2.14.2")

    // RabbitMQ client
    implementation("com.rabbitmq:amqp-client:5.16.0")
}

tasks.test {
    useJUnitPlatform()  // Encourages(?) JUnit 5 use by Kotlin
}
