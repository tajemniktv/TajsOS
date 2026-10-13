---
name: tajsos-gradle-build
description: TajsOS build overlay for Kotlin Multiplatform + Compose Multiplatform projects, including version alignment and repo-valid verification commands.
---

# TajsOS Gradle Build Overlay

## Build-system stance

- TajsOS uses Kotlin Multiplatform and Compose Multiplatform.
- Do not blindly apply Android-only AGP migration guidance to KMP-wide build decisions.
- Treat AGP-specific skills as Android-scoped reference, not the primary KMP migration guide.

## Versioning and config rules

- Prefer version catalogs and centralized build configuration.
- Preserve Kotlin/Compose/AGP/Gradle/JDK alignment when changing build tooling.

## Verification command sets (repo-validated)

- Fast local checks:
    - `./gradlew :shared:jvmTest :composeApp:jvmTest :server:test`
- Android-specific checks:
    - `./gradlew :androidApp:lintDebug :androidApp:assembleDebug`
- Full CI-ish checks:
  -
  `./gradlew :shared:jvmTest :composeApp:jvmTest :server:test :androidApp:lintDebug :androidApp:assembleDebug :koverXmlReport`

`--no-daemon` is CI/full-isolation guidance, not mandatory for normal local runs.
