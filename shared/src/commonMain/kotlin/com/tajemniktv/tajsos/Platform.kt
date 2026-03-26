/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
