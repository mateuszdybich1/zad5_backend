package com.example.routes

import com.example.models.OrderItems
import com.example.models.Orders
import com.example.models.Products
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrderItem(val id: UUID, val quantity: Int)

data class OrderRequest(val items: List<OrderItem>)

fun Route.orderRoutes() {
    authenticate("auth-jwt") {
        post("/orders") {
            val principal = call.principal<JWTPrincipal>()!!
            val userUuid = UUID.fromString(principal.payload.getClaim("userId").asString())
            val req = call.receive<OrderRequest>()

            if (req.items.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, "Empty request")
                return@post
            }

            if (req.items.any { x -> x.quantity <= 0 }) {
                call.respond(HttpStatusCode.BadRequest, "Each product must have quantity bigger than 0")
                return@post
            }

            if (req.items.any { item ->
                    transaction {
                        Products.select { Products.id eq item.id }.empty()
                    }
                }
            ) {
                call.respond(HttpStatusCode.BadRequest, "One or more products do not exist")
                return@post
            }

            try {
                val newOrderId =
                    transaction {
                        val orderId =
                            Orders.insertAndGetId {
                                it[userId] = userUuid
                            }

                        req.items.forEach { item ->
                            OrderItems.insert {
                                it[OrderItems.orderId] = orderId
                                it[productId] = item.id
                                it[quantity] = item.quantity
                            }
                        }
                        orderId.value
                    }

                call.respond(HttpStatusCode.Created, mapOf("orderId" to newOrderId))
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, e.localizedMessage)
            }
        }
    }
}
