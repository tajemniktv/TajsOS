package com.tajemniktv.tajsos

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform