---
title: Tech Stack
description: Languages, frameworks, and tools used in TajsOS.
---

- **Language:** Kotlin
- **App Model:** Kotlin Multiplatform
- **UI:** Compose Multiplatform
- **Design System:** Material 3
- **Architecture:** Pragmatic layered architecture
- **State Management:** ViewModel + StateFlow + immutable UI state
- **Async:** Kotlin Coroutines
- **Persistence:** Room + DataStore
- **Backend:** Ktor (for sync / remote features)
- **Build System:** Gradle Kotlin DSL

## Repository Structure

- `androidApp/`: Android-specific app entrypoint
- `composeApp/`: Shared Compose UI and navigation
- `shared/`: Core data models, entities, repository, business logic
- `server/`: Ktor backend for sync / remote features
- `website/`: Website / documentation built with Astro + Starlight
- `iosApp/`: iOS scaffold
