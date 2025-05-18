package com.example

import com.example.models.OrderItems
import com.example.models.Orders
import com.example.models.Products
import com.example.models.Users
import com.example.routes.authRoutes
import com.example.routes.orderRoutes
import com.example.routes.productRoutes
import com.example.security.JWTConfig
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.routing.routing
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
