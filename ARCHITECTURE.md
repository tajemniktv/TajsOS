# TajsOS Architecture

This document describes the current structural truths, boundaries, and tensions within TajsOS. It is intended to help contributors understand how the app is built today and where its architectural stress points lie.

## 1. Module Boundaries

TajsOS is built as a Kotlin Multiplatform (KMP) project targeting Android, Desktop (JVM), and Server, with scaffolding for iOS and Web.

- **`:shared` (The Core)**
  - Contains all data models (e.g., `NodeEntity`, `TrackEntryEntity`), Room DAOs, and the database definition (`AppDatabase`).
  - Contains `AppRepository`, which acts as the single source of truth for local data.
  - Contains `MainViewModel`, the central state holder for the UI. It is housed here to be shared across platforms.
  - **Dependencies:** Ktor Client, Room, DataStore, Kotlinx Serialization, Coroutines.

- **`:composeApp` (The UI Layer)**
  - Contains all Compose Multiplatform code (screens, components, design system).
  - Handles navigation (`Screen.kt`).
  - Directly consumes StateFlows exposed by `MainViewModel`.
  - **Dependencies:** `:shared`, Compose Multiplatform.

- **`:androidApp` (The Android Host)**
  - A thin wrapper module that sets up Android-specific initialization (e.g., `MainActivity`, biometric authentication, intent handling).
  - **Dependencies:** `:composeApp`, `:shared`, AndroidX.

- **`:server` (The Backend)**
  - A Ktor-based local/remote backend intended for future sync and remote API features.
  - Currently a minimal shell.

## 2. Data Flow & State Ownership

The app follows a unidirectional data flow (UDF) pattern:

1. **Persistence:** `AppDatabase` (via Room) holds the local state.
2. **Repository:** `AppRepository` exposes data via Kotlin `Flow`s (e.g., `getAllActiveNodes()`). It does not hold state itself; it merely queries the database.
3. **ViewModel:** `MainViewModel` collects these flows, performs business logic, and exposes immutable `StateFlow`s to the UI.
4. **UI:** Compose screens observe `StateFlow`s (e.g., `viewModel.activeTasks.collectAsState()`) and render the current state. User actions trigger methods on `MainViewModel`.

### The Core Entity: `NodeEntity`
TajsOS uses a unified entity pattern. Most user data (tasks, notes, projects, areas, ideas) are stored as `NodeEntity` instances in the `nodes` table, distinguished by the `type` column. This allows generic handling of relationships (`RelationEntity`) and tagging across all item types.

## 3. Architectural Truths & Tensions

While the layered architecture works, there are intentional pragmatic tradeoffs and emerging tensions:

### A. The "God Object" ViewModel (`MainViewModel`)
- **Truth:** All UI state and business logic currently flow through a single `MainViewModel` located in `:shared/src/commonMain/kotlin/com/tajemniktv/tajsos/ui/`.
- **Tension:** As the app grows, `MainViewModel` (currently ~2,100 lines) takes on too many responsibilities (calendar sync, biometric lock state, complex insight calculations, generic CRUD).
- **Future Direction:** Do not split this up prematurely unless a specific feature requires isolated state, but recognize it as a bottleneck. Extracting distinct use cases or smaller view models (e.g., `CalendarViewModel`, `InsightsViewModel`) is the logical next step.

### B. Shared Logic vs. UI Location
- **Truth:** `MainViewModel` lives in `:shared/ui/` but is tightly coupled to UI concerns (it depends on `androidx.lifecycle.ViewModel`).
- **Tension:** While standard in KMP to share ViewModels, it blurs the line between the `domain/data` boundary and the `presentation` boundary.
- **Guidance:** Leave `MainViewModel` in `:shared` for cross-platform reuse, but keep Compose-specific imports (e.g., `Color`, `Modifier`) strictly inside `:composeApp`.

### C. Direct Database Filtering vs. In-Memory Filtering
- **Truth:** We rely heavily on Room DAOs to filter data via SQL `WHERE` clauses (e.g., `NodeDao.getAllNodesWithPins()`).
- **Tension:** `MainViewModel` sometimes performs complex in-memory filtering (e.g., insight calculations).
- **Guidance:** Prefer direct database queries over in-memory `Flow` mapping for large datasets. Use `Map` and `remember` in Compose for O(1) lookups during recomposition to avoid N+1 issues.

### D. The Sync Story
- **Truth:** TajsOS is local-first. `AppRepository` writes directly to Room.
- **Tension:** The `:server` module exists for remote sync, but the local data models (`NodeEntity`) do not currently have robust CRDT or timestamp-based conflict resolution mechanisms beyond basic versioning.
- **Guidance:** Future sync work will require significant changes to `AppRepository` to handle local vs. remote state reconciliation.

## 4. Navigation & Entry Points

- `MainActivity.kt` (`:androidApp`): Handles platform intents and biometrics, then defers to `App()`.
- `App.kt` (`:composeApp`): The root composable. Sets up the `NavHost` and scaffold.
- `Screen.kt` (`:composeApp`): A sealed class defining all valid routes and bottom-nav groups.

## Summary for Contributors

1. **Keep UI dumb:** Screens should only display state and emit events to the ViewModel.
2. **Keep data close to the DB:** Use Room relations (`NodeWithPin`) and specific queries rather than fetching everything and filtering in Kotlin.
3. **Small refinements over grand rewrites:** If you see a structural issue (like the large ViewModel), extract small pieces (like helper functions or distinct state classes) rather than attempting to rewrite the entire data layer.
