val kotlin_version: String by project
val logback_version: String by project
val exposedVersion: String by project
val mssqlJdbcVersion: String by project

plugins {
    kotlin("jvm") version "2.1.10"
    id("io.ktor.plugin") version "3.1.2"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
}

group = "com.example"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-config-yaml")
    implementation("io.ktor:ktor-server-auth:$kotlin_version")
    implementation("io.ktor:ktor-server-auth-jwt:$kotlin_version")
    implementation("io.ktor:ktor-server-content-negotiation:$kotlin_version")
    implementation("io.ktor:ktor-serialization-jackson:$kotlin_version")
    implementation("io.ktor:ktor-server-cors:$kotlin_version")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("com.microsoft.sqlserver:mssql-jdbc:$mssqlJdbcVersion")
}
