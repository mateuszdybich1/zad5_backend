package com.example.routes

import com.example.models.Products
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.http.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

data class ProductRequest(val name: String)

fun Route.productRoutes() {
    route("/products") {
        get {
            val list = transaction {
                (Products).selectAll().map {
                    mapOf(
                        "id" to it[Products.id].value.toString(),
                        "name" to it[Products.name]
                    )
                }
            }
            call.respond(list)
        }
        authenticate("auth-jwt") {
            post {
                val req = call.receive<ProductRequest>()
                transaction {
                    Products.insert {
                        it[name] = req.name
                    }
                }
                call.respond(HttpStatusCode.Created, "Product added")
            }
        }
    }
}
