package com.example.routes

import com.example.models.Users
import com.example.security.JWTConfig
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.security.MessageDigest

data class AuthRequest(val username: String, val password: String)

fun Route.authRoutes() {
    post("/register") {
        val req = call.receive<AuthRequest>()

        val userRow =
            transaction {
                Users
                    .select { (Users.username eq req.username) }
                    .limit(1)
                    .firstOrNull()
            }

        if (userRow != null) {
            call.respond(HttpStatusCode.BadRequest, "User already exists")
            return@post
        }

        val hash =
            MessageDigest.getInstance("SHA-256")
                .digest(req.password.toByteArray())
                .joinToString("") { "%02x".format(it) }
        transaction {
            Users.insert {
                it[username] = req.username
                it[password] = hash
            }
        }
        call.respond(HttpStatusCode.Created, "User registered")
    }

    post("/login") {
        val req = call.receive<AuthRequest>()
        val hash =
            MessageDigest.getInstance("SHA-256")
                .digest(req.password.toByteArray())
                .joinToString("") { "%02x".format(it) }

        val userRow =
            transaction {
                Users
                    .select { (Users.username eq req.username) and (Users.password eq hash) }
                    .limit(1)
                    .firstOrNull()
            }

        if (userRow == null) {
            call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
            return@post
        }

        val userId = userRow[Users.id].toString()

        val token = JWTConfig.makeToken(userId)
        call.respond(mapOf("token" to token))
    }
}
