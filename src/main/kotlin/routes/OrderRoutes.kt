package com.example.routes

import com.example.models.Orders
import com.example.models.OrderItems
import io.ktor.server.routing.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.*
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.jetbrains.exposed.sql.insert

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrderItem(val id: UUID, val quantity: Int)

data class OrderRequest(val items: List<OrderItem>)

fun Route.orderRoutes() {
    authenticate("auth-jwt") {
        post("/orders") {
            val principal = call.principal<JWTPrincipal>()!!
            val userUuid  = UUID.fromString(principal.payload.getClaim("userId").asString())
            val req       = call.receive<OrderRequest>()

            try {
                val newOrderId = transaction {
                    val orderId = Orders.insertAndGetId {
                        it[userId] = userUuid
                    }

                    req.items.forEach { item ->
                        OrderItems.insert {
                            it[OrderItems.orderId]   = orderId
                            it[productId] = item.id
                            it[quantity]= item.quantity
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
