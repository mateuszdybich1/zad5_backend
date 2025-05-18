package com.example.models

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object Users : UUIDTable("users") {
    val username = varchar("username", 255)
    val password = varchar("password", 64)
}

object Products : UUIDTable("products") {
    val name       = varchar("name", 100)
}

object Orders : UUIDTable("orders") {
    val userId    = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
}

object OrderItems : UUIDTable("order_items") {
    val orderId    = reference("order_id", Orders, onDelete = ReferenceOption.CASCADE)
    val productId    = reference("product_id", Products, onDelete = ReferenceOption.CASCADE)
    val quantity  = integer("quantity")
}
