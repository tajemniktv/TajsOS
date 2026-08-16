package com.tajemniktv.tajsos

/**
 * WebAssembly-specific implementation of the generic [Platform] interface.
 *
 * Provides runtime information specific to Wasm browser environments.
 */
class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()