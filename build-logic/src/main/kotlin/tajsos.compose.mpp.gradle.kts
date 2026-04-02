/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

import com.tajemniktv.tajsos.buildlogic.stringConfig
import com.tajemniktv.tajsos.buildlogic.tajsosBuild

plugins {
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

val tajsos = tajsosBuild()

composeCompiler {
    stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("compose_compiler_config.conf"))
}

compose.desktop {
    application {
        nativeDistributions {
            packageVersion = stringConfig("compose.packageVersion", tajsos.composePackageVersion)
        }
    }
}
