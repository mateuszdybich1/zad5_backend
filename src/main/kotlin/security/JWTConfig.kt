package com.example.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.config.ApplicationConfig
import java.util.Date

object JWTConfig {
    private lateinit var algorithm: Algorithm
    private lateinit var issuer: String
    private var validityMs: Long = 0

    fun initialize(config: ApplicationConfig) {
        val jwt = config.config("jwt")
        algorithm = Algorithm.HMAC256(jwt.property("secret").getString())
        issuer = jwt.property("issuer").getString()
        validityMs = jwt.property("validity_ms").getString().toLong()
    }

    fun makeToken(userId: String): String =
        JWT.create()
            .withIssuer(issuer)
            .withClaim("userId", userId)
            .withExpiresAt(Date(System.currentTimeMillis() + validityMs))
            .sign(algorithm)

    fun configure(auth: AuthenticationConfig) {
        auth.jwt("auth-jwt") {
            realm = issuer
            verifier(JWT.require(algorithm).withIssuer(issuer).build())
            validate { cred ->
                val uid = cred.payload.getClaim("userId").asString()
                if (uid.isNotEmpty()) JWTPrincipal(cred.payload) else null
            }
        }
    }
}
