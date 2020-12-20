group = "org.jetbrains.research.code-summarization-dataset"
version = "0.0"

plugins {
    application
    kotlin("jvm") version "1.4.10"
    id("io.gitlab.arturbosch.detekt") version "1.14.2"
}

application {
    mainClassName = "MainKt"
}

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test-junit"))
    // reflect
    implementation("org.jetbrains.kotlin", "kotlin-reflect", "1.4.10")
    // fuel
    implementation(group = "com.github.kittinunf.fuel", name = "fuel", version = "2.3.0")
    // json
    implementation(group = "com.fasterxml.jackson.module", name = "jackson-module-kotlin", version = "2.11.3")
    // arguments parser
    implementation("com.github.ajalt.clikt", "clikt", "3.0.1")
}

detekt {
    failFast = true // fail build on any finding
    config = files("detekt.yml")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}
