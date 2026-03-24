# AGENTS.md

## Project identity

TajsOS is a low-friction "Second Brain" multiplatform app for managing your life. It is not
neccessarily for ADHD brains, but it is designed with ADHD brains in mind. It tries to replace
overwhelming lists with a mechanical, satisfying control center that makes task capture and
execution feel like operating heavy machinery.

## Tech stack

- **Targets:** Android + Desktop (JVM) + web (iOS and web targets scaffolded, Android/Desktop
  primary)
- **Language:** Kotlin
- **App model:** Kotlin Multiplatform (KMP)
- **UI:** Compose Multiplatform (CMP, shared UI)
- **Design system:** Material 3
- **Architecture:** Pragmatic layered architecture with shared `data`/`domain` (in `:shared`) and UI
  separated from business logic. Core logic and data models live in
  `shared/src/commonMain/kotlin/com/tajemniktv/tajsos/data/`.
- **Module structure:**
    - `:shared` — core logic, Room entities, repositories, and data models
    - `:composeApp` — shared Compose UI, navigation, and screens for all platforms
    - `:androidApp` — Android-specific entry point
    - `:server` — Ktor-based backend for sync/remote features
- **Calendar integration:** Supports external calendar providers and events (Google, Outlook, ICS,
  etc.)
- **Localization:** The app is being developed with localization in mind; UI strings are sourced for
  translation.
- **State management:** ViewModel + StateFlow + immutable UI state (see `MainViewModel` in
  ComposeApp)
- **Async:** Kotlin coroutines
- **Navigation:** State-driven Compose navigation using a sealed `Screen` class; each major feature
  has a dedicated screen in `composeApp/src/commonMain/kotlin/com/tajemniktv/tajsos/ui/screens/`.
- **Persistence:** Room for structured data, DataStore for preferences/settings
- **Sync:** Local-first approach with a remote backend for cross-device sync
- **Background work:** WorkManager on Android only when it provides clear value
- **Dependency injection:** Manual DI at first, Hilt only if complexity justifies it
- **Build system:** Gradle with Kotlin DSL and version catalogs (`gradle/libs.versions.toml`). All
  modules use plugin aliases and share dependency versions.

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
    - The main entity is `NodeEntity` (unified model for `task`, `note`, `idea`, `project`, `area`,
      `resource`, and LifeOS types like `open_loop`, `decision`, `maintenance`, `protocol`,
      `person`,
      etc.).
    - Operating Modes are implemented via `ModeEntity`, `ModePreferenceEntity`,
      `ModeAreaFilterEntity`, `ModeTypeFilterEntity`, and `ModeUsageLogEntity` for context-driven
      UI,
      filtering, and user workflows. Modes like "COMMAND", "FOCUS", and "RECOVERY" are seeded and
      used
      throughout the app.
    - Relations are handled via `RelationEntity` for linking nodes (e.g., tasks to projects).
    - "Today" is implemented as `TodayPinEntity` (table for daily pinning).
    - Focus sessions and daily tracking: `FocusSessionEntity`, `TrackEntryEntity`.
    - Templates: `TemplateEntity` for reusable item structures.
    - Reviews: `ReviewEntity` for formal reflection sessions.
    - Snapshots: `NodeSnapshotEntity` for versioning node content.
    - Attachments: `AttachmentEntity` for files/links associated with nodes.
    - Calendar: `CalendarProviderEntity` and `CalendarEventEntity` for external calendar
      integration.
- **Repository pattern:**
    - All data access is funneled through `AppRepository` in `shared`.
    - ViewModels (e.g., `MainViewModel`) expose StateFlows for UI state.
    - AppRepository also provides flows for calendar, template, review, snapshot, and attachment
      data.
- **Status/type conventions:**
    - `NodeEntity` uses `type` (`task`, `note`, `project`, `area`, etc.) and `status` (`active`,
      `done`, `archived`, `on_hold`, `someday`, `blocked`).
    - `ReviewEntity` uses `type` (`daily`, `weekly`, `monthly`).
    - `TemplateEntity` uses `nodeType` (`task`, `note`, `project`).
- **Main entrypoints:**
    - UI: `App.kt` in `composeApp/src/commonMain/kotlin/com/tajemniktv/tajsos/`
    - Data: `AppRepository` and entities in
      `shared/src/commonMain/kotlin/com/tajemniktv/tajsos/data/`
    - Insights/stats: `MainViewModel` exposes a rich `insights` StateFlow for weekly summaries,
      context switching, backlog pressure, and LifeOS-specific metrics (e.g., mood/focus
      correlations,
      area health, context stability, project entropy, and other context-driven insights).
    - Advanced search/filtering: `MainViewModel` supports multi-criteria search (by tag, type,
      status,
      project, area, energy, friction, etc.).
    - Biometric/locking: App supports biometric authentication and locking via preferences and
      ViewModel state.
    - Export: Data export to JSON is available via ViewModel.

## Testing Guidelines

- **Run all tests**: `./gradlew test`
- **Run `:shared` tests (JVM)**: `./gradlew :shared:cleanTest :shared:jvmTest`
- **Run specific `:shared` test (JVM)**: `./gradlew :shared:testJvmTest --tests "com.tajemniktv.tajsos.ClassName"`
- **Run `:composeApp` tests (JVM)**: `./gradlew :composeApp:jvmTest`
- **Run `:server` tests**: `./gradlew :server:test`
- **Testing stack**:
  - `app.cash.turbine:turbine` for testing flows.
  - `org.jetbrains.kotlinx:kotlinx-coroutines-test` for coroutines. Note: check `gradle/libs.versions.toml` for `kotlinx.coroutines.test` vs `kotlinx.coroutinesTest` name clashes before modifying versions.
  - `io.mockk:mockk` for mocking in `commonTest`/`jvmTest`.

## Build & Environment Quirks

- **Gradle daemon hangs**: If the build environment hangs at the task graph calculation phase, try `gradle --no-daemon` or `--no-configuration-cache`, and ensure Java processes from previous builds are killed.
- **Executing wrapper**: Make sure to run `chmod +x gradlew` if needed. Use the local `gradle` binary if wrapper download timeouts occur.
- **Compose Multiplatform compile (JVM)**: Use `./gradlew :composeApp:compileKotlinJvm` as the generic `compileKotlin` task can be ambiguous.
- **Formatting**: `ktlintCheck` is not currently available or configured in the root project. Do not try to run it.

## Kotlin & Code Rules

- **Empty collections average**: `.average()` on an empty numerical collection returns `Double.NaN`. When using `.mapNotNull()`, ensure you check `.isNotEmpty()` on the mapped collection, not the original, to avoid silent NaN propagation.
- **Kotlin snippets**: Standalone `kotlinc` is not available. To test Kotlin, wrap in a temporary test file in `shared/src/commonTest/kotlin` and execute via Gradle.

## Architecture & Logic Specifics

- **Android Manifest**: The app intentionally leaves backups enabled (`android:allowBackup="true"`) during development. Do not disable this for security.
- **Intents Processing**: In `MainActivity.kt`, intents (like `ACTION_SEND`) must not be processed in `onCreate`/`onNewIntent` before biometric authentication (i.e., `isAuthenticated == true`).
- **NodeStatus Side Effects**: In `MainViewModel.updateNodeStatus`, marking a recurring node as "done" automatically triggers calculating the next due date and inserting a new active node.
- **Database/Room Performance**: Prefer direct database queries (DAO methods with WHERE clauses) over in-memory filtering of large data streams (Flows) in the ViewModel.
- **Relations/DAOs**:
  - `NodeDao.getAllNodesWithPins()` excludes nodes with `status = 'archived'`. Downstream flows won't see archived nodes.
  - DAO methods returning combined models like `NodeWithPin` (which combines `NodeEntity`, `TodayPinEntity`, `TagEntity`) must be marked with `@Transaction`.
- **Server Binding**: The Ktor server in `:server` binds to `127.0.0.1` by default for security. Override with the `SERVER_HOST` env variable.
- **Compose UI**:
  - Empty lists should use the `EmptyState` composable (`com.tajemniktv.tajsos.ui.components.common.EmptyState`).
  - To optimize recomposition, avoid N+1 list lookups. Pre-compute data into a Map (e.g., `associateBy`) and cache with `remember(state)`.
  - Action buttons (e.g., Save/Create) should use the `enabled` parameter to reflect invalid form states rather than silently ignoring the click.
