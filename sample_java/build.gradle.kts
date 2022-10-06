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

    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    mavenLocal()
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Needs rivers framework to operate
    implementation(project(":river"))
}
