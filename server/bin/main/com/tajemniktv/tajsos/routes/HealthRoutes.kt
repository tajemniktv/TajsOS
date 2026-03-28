package com.tajemniktv.tajsos.routes

import com.tajemniktv.tajsos.dto.HealthResponse
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.healthRoutes() {
    val startTime = System.currentTimeMillis()

    get("/health") {
        call.respond(
            HealthResponse(
                status = "OK",
                version = "1.0.0", // Hardcoded for now, could be passed from build
                uptime = System.currentTimeMillis() - startTime
            )
        )
    }
}
