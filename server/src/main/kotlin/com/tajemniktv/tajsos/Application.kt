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

fun Application.module() {
    val expectedToken = environment.config.propertyOrNull("TAJSOS_SYNC_TOKEN")?.getString()
        ?: System.getenv("TAJSOS_SYNC_TOKEN")
        ?: error("TAJSOS_SYNC_TOKEN environment variable must be set")

    val expectedTokenHash = MessageDigest.getInstance("SHA-256").digest(expectedToken.toByteArray(Charsets.UTF_8))

    install(Authentication) {
        bearer("sync-auth") {
            authenticate { tokenCredential ->
                if (MessageDigest.isEqual(tokenCredential.token.toByteArray(Charsets.UTF_8), expectedToken.toByteArray(Charsets.UTF_8))) {

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
