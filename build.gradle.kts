/*
 * Copyright (c) 2022 by Fred George
 * May be used freely except for training; license required for training.
 * @author Fred George  fredgeorge@acm.org
 */

repositories {
    // Use Maven Local for personal tools to use
    mavenLocal()

    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    kotlin("jvm") version "1.6.10"
}
