package com.example

import com.example.models.*
import com.example.routes.*
import com.example.security.JWTConfig
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun main(args: Array<String>) {
    EngineMain.main(args)
}

@Suppress("unused")
fun Application.module() {
    JWTConfig.initialize(environment.config)

    val db = environment.config.config("database")
    Database.connect(
        url = db.property("url").getString(),
        driver = db.property("driver").getString(),
        user = db.property("user").getString(),
        password = db.property("password").getString(),
    )
    transaction {
        SchemaUtils.create(Users, Products, Orders, OrderItems)
    }

    install(ContentNegotiation) { jackson() }
    install(CORS) {
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        anyHost()
    }
    install(Authentication) { JWTConfig.configure(this) }

    routing {
        authRoutes()
        productRoutes()
        orderRoutes()
    }
}
