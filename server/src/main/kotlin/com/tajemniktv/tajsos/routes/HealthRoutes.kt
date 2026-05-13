package com.tajemniktv.tajsos.routes

import com.tajemniktv.tajsos.dto.HealthResponse
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.healthRoutes() {
    val startTime = System.currentTimeMillis()

    get("/health") {
        val version = application.environment.config.propertyOrNull("ktor.application.version")?.getString()
            ?: "1.0.0"

        call.respond(
            HealthResponse(
                status = "OK",
                version = version,
                uptime = System.currentTimeMillis() - startTime
            )
        )
    }
}
