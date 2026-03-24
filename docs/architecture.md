# TajsOS Architecture

TajsOS is built using **Kotlin Multiplatform (KMP)** and **Compose Multiplatform (CMP)**. It follows a pragmatic layered architecture that isolates business rules, data access, and UI logic, enabling code sharing across multiple targets (Android and Desktop JVM primarily).

## 1. High-Level Layers

### Core/Domain Layer (`:shared`)
- **What it is:** The central source of truth for the application. It contains all data models (`*Entity`), repositories, and DAOs.
- **Technologies:** Room (via KMP implementation), Ktor Client, DataStore (for preferences).
- **Rule:** This layer must remain independent of any UI logic. It exposes data via Kotlin `Flow` and suspend functions.

### Presentation Layer (`:composeApp`)
- **What it is:** The visual representation of the app and user interaction handling.
- **Technologies:** Compose Multiplatform, ViewModels, Material 3.
- **Rule:** ViewModels should hold state (`StateFlow`), process intents from the UI, and interact with the Repository in the Core Layer. It should not deal with direct database queries.

### Platform-Specific Entry Points (`:androidApp`, `:iosApp`, Desktop `MainKt`)
- **What it is:** The native wrappers that initialize the application.
- **Rule:** Keep these as thin as possible. They exist mostly for configuring the window, handling permissions, and bootstrapping the Compose application.

---

## 2. Data Flow (Unidirectional Data Flow)

TajsOS relies on the classic Unidirectional Data Flow (UDF) pattern:

```text
[ Compose UI ]  -- (User Intent / Event) --> [ ViewModel ]
       ^                                         |
       |                                         | (Suspend Function / Update)
       |                                         v
       |                                 [ Repository ]
(StateFlow / Flow)                               |
       |                                         | (Room DAO / Ktor API)
       |                                         v
       +-------------------------------- [ Data Source ]
```

1. **State:** The ViewModel holds an immutable state representation (`StateFlow<UiState>`). The Compose UI observes this state and recomposes when it changes.
2. **Event:** When a user interacts (e.g., clicks "Save Node"), the UI fires an event/intent to the ViewModel.
3. **Logic:** The ViewModel processes the intent, updates internal state (e.g., showing a loading indicator), and delegates persistence to the `AppRepository`.
4. **Data:** `AppRepository` invokes the required Room DAOs.
5. **Update:** The database updates its tables. Room DAOs returning `Flow` automatically emit the new data.
6. **Recompose:** The Repository `Flow` feeds into the ViewModel's `StateFlow`, which in turn triggers a Compose recomposition, reflecting the new state to the user.

---

## 3. Major Architectural Decisions & Tradeoffs

- **Single Unified Entity (`NodeEntity`):** Instead of having separate tables for Tasks, Notes, Projects, and Ideas, they are all unified into one table (`nodes`).
  - *Tradeoff:* Simplifies the sync engine and relationship mapping (`RelationEntity`), but can lead to sparse columns (a note doesn't need a due date) and complex filtering logic in the UI layer.
- **Direct Database Queries for Filtering:** The app prefers direct SQL `WHERE` clauses (via DAOs) over fetching large sets of data and filtering them in memory using Kotlin sequences or flows.
  - *Tradeoff:* Improves runtime performance and memory usage, but couples specific UI queries directly to the Room DAO definitions.
- **Local-First with Ktor Server:** The system is designed to be fully functional without an internet connection. The `:server` acts as a local sync relay rather than a traditional cloud backend.
  - *Tradeoff:* Offers complete privacy and offline capability, but requires implementing complex, conflict-free sync algorithms to handle cross-device data merging.
- **Compose Multiplatform (CMP) over Native UIs:** The UI is shared entirely via Compose Multiplatform.
  - *Tradeoff:* Greatly speeds up development for Android and Desktop. However, iOS and Web targets may experience "uncanny valley" effects or lack native accessibility/navigation features.
