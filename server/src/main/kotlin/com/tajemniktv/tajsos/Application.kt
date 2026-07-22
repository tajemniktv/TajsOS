package com.tajemniktv.tajsos

import java.security.MessageDigest

import com.tajemniktv.tajsos.routes.healthRoutes
import com.tajemniktv.tajsos.routes.syncRoutes
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.auth.*

import kotlinx.serialization.json.Json

fun main() {
    val host = System.getenv("SERVER_HOST") ?: "127.0.0.1"
    embeddedServer(Netty, port = SERVER_PORT, host = host, module = Application::module)
        .start(wait = true)
}

/**
 * Initializes the Ktor application with sync authentication and JSON content negotiation.
 *
 * The sync API requires the `TAJSOS_SYNC_TOKEN` environment variable (or config property)
 * to be set. The module installs bearer auth named `"sync-auth"` and validates provided
 * tokens by hashing them with SHA-256, comparing against the expected token hash using
 * constant-time equality. Tokens must be high entropy and are never logged.
 */
fun Application.module() {
    val expectedToken = environment.config.propertyOrNull("TAJSOS_SYNC_TOKEN")?.getString()
        ?: System.getenv("TAJSOS_SYNC_TOKEN")
        ?: error("TAJSOS_SYNC_TOKEN environment variable must be set")

    require(expectedToken.length >= 16) {
        "TAJSOS_SYNC_TOKEN must be at least 16 characters long to satisfy entropy enforcement rules."
    }

    val expectedTokenHash =
        MessageDigest
            .getInstance("SHA-256")
            .digest(expectedToken.toByteArray(Charsets.UTF_8))

    install(Authentication) {
        bearer("sync-auth") {
            authenticate { tokenCredential ->
                val providedTokenHash =
                    MessageDigest
                        .getInstance("SHA-256")
                        .digest(tokenCredential.token.toByteArray(Charsets.UTF_8))
                if (MessageDigest.isEqual(providedTokenHash, expectedTokenHash)) {
                    UserIdPrincipal("sync-client")
                } else {
                    null
                }
            }
        }
    }

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }

        healthRoutes()
        syncRoutes()
    }
}
