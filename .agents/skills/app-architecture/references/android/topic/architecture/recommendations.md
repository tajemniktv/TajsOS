# Architecture Recommendations

Source: https://developer.android.com/topic/architecture/recommendations

Priority levels:

- **Strongly recommended**: Implement unless fundamentally clashing with your approach
- **Recommended**: Likely to improve your app
- **Optional**: Can improve your app in certain circumstances

---

## Layered Architecture

**Use a clearly defined data layer** -- Strongly recommended. Contains business logic, exposes
application data, create repositories even for single data sources.

**Use a clearly defined UI layer** -- Strongly recommended. Displays application data, serves as
primary user interaction point. Use Jetpack Compose.

**Do not allow UI components to interact directly with data sources** -- Strongly recommended. No
direct access to databases, DataStore, SharedPreferences, Firebase APIs, GPS, Bluetooth, network
connectivity providers.

**Use coroutines and flows** -- Strongly recommended. Communicate between layers using coroutines
and flows.

**Use a domain layer** -- Recommended in big apps. Add use cases when reusing business logic across
ViewModels or simplifying ViewModel complexity.

---

## UI Layer

**Follow Unidirectional Data Flow** -- Strongly recommended. ViewModels expose UI state using
observer pattern.

**Use AAC ViewModels** -- Strongly recommended. Handle business logic and fetch application data.

**Use lifecycle-aware UI state collection** -- Strongly recommended. Use
`collectAsStateWithLifecycle()`.

**Do not send events from ViewModel to UI** -- Strongly recommended. Process events immediately in
ViewModel and cause state update.

**Use a single-activity application** -- Strongly recommended. Use Navigation 3.

**Use Jetpack Compose** -- Strongly recommended.

---

## ViewModel

**Keep ViewModels independent of Android lifecycle** -- Strongly recommended. Don't hold references
to lifecycle-related types.

**Use coroutines and flows** -- Strongly recommended. Use Kotlin flows for receiving data, suspend
functions for performing actions.

**Use ViewModels at screen level** -- Strongly recommended. Don't use in reusable UI pieces.

**Use plain state holder classes in reusable UI components** -- Strongly recommended.

**Do not use AndroidViewModel** -- Recommended. Use ViewModel class instead.

**Expose a UI state** -- Recommended. Use StateFlow with `stateIn(WhileSubscribed(5_000))`.

---

## Lifecycle

**Use lifecycle-aware effects** -- Strongly recommended. Use `LifecycleStartEffect`,
`LifecycleResumeEffect`, `repeatOnLifecycle`, `collectAsStateWithLifecycle`.

---

## Dependency Injection

**Use dependency injection** -- Strongly recommended. Use constructor injection when possible.

**Scope to a component when necessary** -- Strongly recommended. Scope when type contains mutable
data that needs to be shared, or type is expensive to initialize.

**Use Hilt** -- Recommended. Use Hilt if project is complex enough (multiple screens with
ViewModels, WorkManager, nav-scoped ViewModels).

---

## Testing

**Know what to test** -- Strongly recommended. At minimum: unit tests for ViewModels (including
Flows), unit tests for data layer entities, UI navigation tests.

**Prefer fakes to mocks** -- Strongly recommended.

**Test StateFlows** -- Strongly recommended. Assert on `.value` property when possible.

---

## Models

**Create a model per layer in complex apps** -- Recommended. Remote data source maps network model
to simpler class. Repositories map DAO models to simpler data classes. ViewModel includes data layer
models in UiState classes.

---

## Naming Conventions

**Methods**: verb phrases (e.g., `makePayment()`) -- Optional.

**Properties**: noun phrases (e.g., `inProgressTopicSelection`) -- Optional.

**Streams**: `get{model}Stream` (e.g., `getAuthorStream(): Flow<Author>`) -- Optional.

**Interface implementations**: meaningful names or `Default` prefix. Fake implementations prefixed
with `Fake` -- Optional.
