/*
 * Copyright (c) 2022 by Fred George
 * @author Fred George  fredgeorge@acm.org
 * Licensed under the MIT License; see LICENSE file in root.
 */

repositories {
    // Use Maven Local for personal tools to use
    mavenLocal()

    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    kotlin("jvm") version "1.9.23"
}
