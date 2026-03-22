# AGENTS.md

## Project identity

TajsOS is a low-friction "Second Brain" multiplatform app for managing your life. It is not
neccessarily for ADHD brains, but it is designed with ADHD brains in mind. It tries to replace
overwhelming lists with a mechanical, satisfying control center that makes task capture and
execution feel like operating heavy machinery.

## Tech stack

- **Targets:** Android + Desktop (JVM) + web (iOS and web targets scaffolded, Android/Desktop primary)
- **Language:** Kotlin
- **App model:** Kotlin Multiplatform (KMP)
- **UI:** Compose Multiplatform (CMP, shared UI)
- **Design system:** Material 3
- **Architecture:** Pragmatic layered architecture with shared `data`/`domain` (in `:shared`) and UI separated from business logic. Core logic and data models live in `shared/src/commonMain/kotlin/com/tajemniktv/tajsos/data/`.
- **Module structure:**
  - `:shared` — core logic, Room entities, repositories, and data models
  - `:composeApp` — shared Compose UI, navigation, and screens for all platforms
  - `:androidApp` — Android-specific entry point
  - `:server` — Ktor-based backend for sync/remote features
- **Calendar integration:** Supports external calendar providers and events (Google, Outlook, ICS,
  etc.)
- **Localization:** The app is being developed with localization in mind; UI strings are sourced for
  translation.
- **State management:** ViewModel + StateFlow + immutable UI state (see `MainViewModel` in ComposeApp)
- **Async:** Kotlin coroutines
- **Navigation:** State-driven Compose navigation using a sealed `Screen` class; each major feature has a dedicated screen in `composeApp/src/commonMain/kotlin/com/tajemniktv/tajsos/ui/screens/`.
- **Persistence:** Room for structured data, DataStore for preferences/settings
- **Sync:** Local-first approach with a remote backend for cross-device sync
- **Background work:** WorkManager on Android only when it provides clear value
- **Dependency injection:** Manual DI at first, Hilt only if complexity justifies it
- **Build system:** Gradle with Kotlin DSL and version catalogs (`gradle/libs.versions.toml`). All modules use plugin aliases and share dependency versions.

---

## Agent behavior expectations

When working in this repo, agents should:

1. Preserve existing behavior unless asked to change it.
2. Explain tradeoffs clearly when making structure decisions.
3. Prefer editing existing files over creating unnecessary new ones.
4. Keep diffs focused and reviewable.
5. Update docs when the product scope or setup changes.
6. Avoid broad rewrites unless explicitly requested.

---

## Documentation expectations

When making meaningful changes, keep these current:
  - `README.md` for product and setup
  - `AGENTS.md` for agent behavior and project rules
  - `ROADMAP.md` for phased direction when scope evolves

---

## Additional project conventions and patterns

- **Data model conventions:**
  - The main entity is `NodeEntity` (unified model for `task`, `note`, `idea`, `project`, `area`, `resource`).
  - Relations are handled via `RelationEntity` for linking nodes (e.g., tasks to projects).
  - "Today" is implemented as `TodayPinEntity` (table for daily pinning).
  - Focus sessions and daily tracking: `FocusSessionEntity`, `TrackEntryEntity`.
  - Templates: `TemplateEntity` for reusable item structures.
  - Reviews: `ReviewEntity` for formal reflection sessions.
  - Snapshots: `NodeSnapshotEntity` for versioning node content.
  - Attachments: `AttachmentEntity` for files/links associated with nodes.
  - Calendar: `CalendarProviderEntity` and `CalendarEventEntity` for external calendar integration.
- **Repository pattern:**
  - All data access is funneled through `AppRepository` in `shared`.
  - ViewModels (e.g., `MainViewModel`) expose StateFlows for UI state.
  - AppRepository also provides flows for calendar, template, review, snapshot, and attachment data.
- **Status/type conventions:**
  - `NodeEntity` uses `type` (`task`, `note`, `project`, `area`, etc.) and `status` (`active`, `done`, `archived`, `on_hold`, `someday`, `blocked`).
  - `ReviewEntity` uses `type` (`daily`, `weekly`, `monthly`).
  - `TemplateEntity` uses `nodeType` (`task`, `note`, `project`).
- **Main entrypoints:**
  - UI: `App.kt` in `composeApp/src/commonMain/kotlin/com/tajemniktv/tajsos/`
  - Data: `AppRepository` and entities in `shared/src/commonMain/kotlin/com/tajemniktv/tajsos/data/`
  - Insights/stats: `MainViewModel` exposes a rich `insights` StateFlow for weekly summaries,
    context switching, backlog pressure, etc.
  - Advanced search/filtering: `MainViewModel` supports multi-criteria search (by tag, type, status,
    project, area, energy, friction, etc.).
  - Biometric/locking: App supports biometric authentication and locking via preferences and
    ViewModel state.
  - Export: Data export to JSON is available via ViewModel.

