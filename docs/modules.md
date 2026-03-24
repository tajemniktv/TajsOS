# TajsOS Module Breakdown

TajsOS is divided into multiple Gradle modules to separate concerns and support multiplatform development. Here is the high-level layout:

```text
TajsOS/
├── shared/         (Core Business Logic & Data Models)
├── composeApp/     (Shared UI & ViewModels)
├── androidApp/     (Android Application Entry Point)
├── iosApp/         (iOS Native Entry Point - Scaffolded)
├── server/         (Ktor Backend Application)
└── website/        (Astro Marketing/Documentation Site)
```

## 1. `:shared`

This module is the backbone of the application. It contains all the platform-independent business logic, data structures, and database interactions.

- **Primary Stack:** Kotlin Multiplatform, Room (KMP), kotlinx.coroutines, kotlinx.serialization, Ktor Client.
- **Responsibilities:**
  - Defining Room entities (`AppDatabase.kt`, `Entities.kt`, `Daos.kt`).
  - Handling preferences via DataStore (`PreferencesRepository.kt`).
  - Providing the single source of truth repository (`Repository.kt`).
  - Making external network requests if necessary.
- **Rules:** No UI imports (`androidx.compose.*` or similar) are allowed here. Only core Kotlin libraries and specified KMP libraries.

## 2. `:composeApp`

This module contains the entire user interface and presentation logic, shared across all supported platforms via Compose Multiplatform.

- **Primary Stack:** Compose Multiplatform (CMP), ViewModels, Material 3.
- **Responsibilities:**
  - Defining UI screens (`ui/screens/`).
  - Managing application navigation.
  - Creating reusable UI components (`ui/components/`, `design/`).
  - Handling user interaction and state via ViewModels (`MainViewModel`).
- **Entry Point (Desktop):** The Desktop (JVM) specific entry point is located here in `jvmMain` (typically `MainKt`).

## 3. `:androidApp`

This module is a thin wrapper that packages the shared UI and logic into an Android APK/AAB.

- **Primary Stack:** Android SDK.
- **Responsibilities:**
  - Android Manifest definition (`AndroidManifest.xml`).
  - Activity entry point (`MainActivity.kt`).
  - Handling platform-specific intents (e.g., Share Sheet support via `ACTION_SEND`).
  - Basic platform integrations like biometric authentication requests.

## 4. `:iosApp`

Currently scaffolded. This module serves as the iOS counterpart to `:androidApp`.

- **Responsibilities:**
  - Initializing the iOS application lifecycle.
  - Bootstrapping the Compose Multiplatform UI via `MainViewController`.

## 5. `:server`

This module is a standalone backend application built with Ktor.

- **Primary Stack:** Ktor Server, Netty, Kotlin Multiplatform dependencies from `:shared`.
- **Responsibilities:**
  - Handling local or remote sync requests from the client apps.
  - Routing and endpoint definition.
  - (Planned) Handling conflict resolution and data merging.
- **Notes:** By default, it binds to `127.0.0.1`. See [Server Documentation](./server.md) for details.

## 6. `website/`

A documentation and marketing site built with Astro.

- **Primary Stack:** Node.js, Astro.
- **Responsibilities:** Providing user-facing documentation, landing pages, and potentially public API docs.
