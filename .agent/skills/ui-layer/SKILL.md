---
name: ui-layer
description: >-
  Google's recommended UI layer architecture for Android. Use this skill when
  designing, implementing, or reviewing ViewModels, UI state, Compose screens,
  state collection, state production, lifecycle effects, UI events, navigation,
  or any UI-layer code. Based on developer.android.com/topic/architecture/ui-layer,
  the events, stateholders, and state-production sub-pages, and the official
  Android architecture recommendations page.
license: Complete terms in LICENSE.txt
metadata:
  author: Google LLC
  keywords:
    - android
    - architecture
    - ui-layer
    - viewmodel
    - ui-state
    - unidirectional-data-flow
    - compose
    - stateflow
    - lifecycle
    - state-holder
    - navigation
    - collectAsStateWithLifecycle
---

# Android Architecture -- UI Layer

**Source of truth:
** [developer.android.com/topic/architecture/ui-layer](https://developer.android.com/topic/architecture/ui-layer)

## TajsOS Scope Note

This skill is Android UI-layer guidance. In TajsOS, preserve AppShell/ScreenScaffold screen
architecture and treat Android-specific patterns as references for Android surfaces only.

## References

- *[UI layer](references/android/topic/architecture/ui-layer.md)* -- full UI layer documentation
- *[UI events](references/android/topic/architecture/ui-layer-events.md)* -- event handling,
  one-time effects, navigation events
- *[State holders](references/android/topic/architecture/ui-layer-stateholders.md)* -- ViewModel vs
  plain state holder, dependency rules
- *[State production](references/android/topic/architecture/ui-layer-state-production.md)* -- state
  production pipeline, APIs, initialization

---

## The UI Layer's Job

Display application data on screen. Accept user interactions. Forward interactions
as events. Nothing else -- no business logic, no direct data source access.

```
User interaction
      |  (event flows up)
      v
ViewModel.onEvent()
      |  (calls data/domain layer)
      v
New UiState emitted
      |  (state flows down)
      v
Composable re-renders
```

---

## Unidirectional Data Flow (UDF) -- **Strongly Recommended**

- State flows **down** from ViewModel to UI
- Events flow **up** from UI to ViewModel
- ViewModels expose UI state via the **observer pattern**; UI never pushes state into ViewModel

**Do not send events from the ViewModel back to the UI.** Process events immediately
in the ViewModel and emit a new state with the result. This ensures:

- Data consistency (single source of truth for UI)
- Reproducibility after configuration changes
- UI actions are never lost

---

## Handling UI Events

### User events

| Scenario                                        | Handler        | Pattern                                        |
|-------------------------------------------------|----------------|------------------------------------------------|
| UI-only state change (expand/collapse, scroll)  | Composable     | Modify local state directly                    |
| Business logic required (save, validate, fetch) | ViewModel      | Call ViewModel function; VM updates UiState    |
| RecyclerView / LazyColumn item actions          | UI + ViewModel | Pass lambda in UiState item, not the ViewModel |

ViewModel functions handling user events use verbs: `addBookmark(id)`, `logIn(username, password)`.

### ViewModel events -- **Always model as state, NEVER as Channel/SharedFlow**

The official docs **strongly recommend against** using `Channel`, `SharedFlow`, or
any reactive stream to send one-time events from ViewModel to UI. These solutions
**don't guarantee delivery and processing**:

- The ViewModel (producer) can outlive the UI (consumer)
- Events can be lost during configuration changes
- No delivery guarantee leaves the app in an inconsistent state

**Instead, model one-time effects as UI state.** The UI notifies the ViewModel when
the effect has been consumed:

```kotlin
data class LatestNewsUiState(
    val news: List<News> = emptyList(),
    val isLoading: Boolean = false,
    val userMessage: String? = null,    // one-time message modeled as state
)

class LatestNewsViewModel(...) : ViewModel() {
    private val _uiState = MutableStateFlow(LatestNewsUiState(isLoading = true))
    val uiState: StateFlow<LatestNewsUiState> = _uiState

    fun refreshNews() {
        viewModelScope.launch {
            if (!internetConnection()) {
                _uiState.update { it.copy(userMessage = "No Internet connection") }
                return@launch
            }
            // fetch news...
        }
    }

    // Called by UI after showing the message
    fun userMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
```

**Compose consumption:**

```kotlin
@Composable
fun LatestNewsScreen(
    snackbarHostState: SnackbarHostState,
    viewModel: LatestNewsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Show message and notify ViewModel when consumed
    uiState.userMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            viewModel.userMessageShown()
        }
    }

    // Rest of UI...
}
```

### Navigation events

Navigation triggered by business logic (e.g., login success) follows the same
state-based pattern:

```kotlin
data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isUserLoggedIn: Boolean = false,  // navigation trigger as state
)

// In Compose:
@Composable
fun LoginScreen(
    onUserLogIn: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val currentOnUserLogIn by rememberUpdatedState(onUserLogIn)
    LaunchedEffect(viewModel, lifecycle) {
        snapshotFlow { viewModel.uiState }
            .filter { it.isUserLoggedIn }
            .flowWithLifecycle(lifecycle)
            .collect { currentOnUserLogIn() }
    }
}
```

User-initiated navigation that needs no business logic (e.g., tapping a "Help" button)
is handled directly in the UI -- no ViewModel involvement needed.

---

## ViewModel -- **Strongly Recommended**

### What ViewModel is responsible for

- Holding and producing UI state
- Handling business logic for the screen
- Fetching app data (via repositories or use cases) to populate `uiState`
- Surviving configuration changes

### Lifecycle independence -- **Strongly Recommended**

Never hold a reference to `Activity`, `Fragment`, `Context`, or `Resources` in a ViewModel.
If a ViewModel appears to need `Context`, the dependency likely belongs in the data layer.

```kotlin
// BAD -- Never do this
class MyViewModel(private val context: Context) : ViewModel()

// GOOD -- Move the Android dependency to data layer, inject as abstraction
class MyViewModel @Inject constructor(
    private val myRepository: MyRepository,
) : ViewModel()
```

Do **not** use `AndroidViewModel`. Move `Application`-dependent logic to the UI or data layer.

### Screen-level only -- **Strongly Recommended**

Use ViewModels only at:

- Screen-level composables
- Activities / Fragments
- Navigation destinations or graphs

**Do not** use ViewModels in reusable UI components. Use plain state holder classes there instead.

### Do not pass ViewModel to child composables -- **Strongly Recommended**

Never pass a ViewModel instance down to other composable functions. This:

- Couples the composable with the ViewModel type
- Makes it less reusable and harder to test/preview
- Creates multiple sites that can call ViewModel functions, making bugs harder to trace

Instead, pass only the needed state down and pass events up as lambdas:

```kotlin
// BAD
@Composable
fun ArticleItem(viewModel: NewsViewModel, article: Article) {
    // composable is now coupled to NewsViewModel
}

// GOOD
@Composable
fun ArticleItem(
    article: ArticleUi,
    onBookmark: () -> Unit,  // event passed up
) {
    // composable is reusable and testable
}
```

---

## State Holders

The UI layer has two types of state holders:

### Business logic state holder (ViewModel)

- Produces screen UI state from data/domain layers
- Survives `Activity` recreation (configuration changes)
- Possesses long-lived state (scoped to navigation destination)
- Unique to a specific screen -- **not reusable**
- Uses constructor injection via Hilt

### UI logic state holder (plain class)

- Manages UI element state and UI-specific logic
- Does **not** survive `Activity` recreation (tied to UI lifecycle)
- Can reference UI-scoped data sources (Context, Resources, NavController)
- **Reusable** across multiple UIs

```kotlin
@Stable
class NiaAppState(
    val navController: NavHostController,
    val windowSizeClass: WindowSizeClass,
) {
    val shouldShowBottomBar: Boolean
        get() = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact

    val shouldShowNavRail: Boolean
        get() = !shouldShowBottomBar

    fun navigate(destination: NiaNavigationDestination) { /* ... */ }
}

@Composable
fun rememberNiaAppState(
    navController: NavHostController = rememberNavController(),
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass,
): NiaAppState = remember(navController, windowSizeClass) {
    NiaAppState(navController, windowSizeClass)
}
```

### State holder dependency rules

State holders can depend on other state holders only if the dependency has **equal or shorter
lifetime**:

| Dependency direction                             | Valid?                        |
|--------------------------------------------------|-------------------------------|
| UI logic holder -> UI logic holder               | Yes                           |
| Screen-level holder (VM) -> UI logic holder      | Yes                           |
| UI logic holder -> Screen-level holder (VM)      | **No** -- outlives dependency |
| Screen-level holder -> different screen's holder | **No** -- different lifecycle |

**Never pass a ViewModel into a plain state holder.** Pass only the needed data as parameters:

```kotlin
// BAD
class MyScreenState(private val viewModel: MyScreenViewModel)

// GOOD
class MyScreenState(
    private val someState: StateFlow<SomeState>,
    private val doSomething: () -> Unit,
)
```

### Choosing between them

**Business logic must be applied BEFORE UI logic** in the state production pipeline.
This is a critical ordering rule.

---

## Exposing UI State -- **Recommended**

### UiState naming convention

Name: `[functionality] + UiState` (e.g., `NewsUiState`, `LoginUiState`, `NewsItemUiState`).

### StateFlow (works with Views and Compose):

```kotlin
class NewsViewModel(private val repository: NewsRepository) : ViewModel() {

    // Option A: stream from data layer -> stateIn with WhileSubscribed(5000)
    val uiState: StateFlow<NewsUiState> =
        repository.getNewsResourcesStream()
            .map { it.toUiState() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = NewsUiState.Loading,
            )

    // Option B: no stream -- MutableStateFlow exposed as immutable
    private val _uiState = MutableStateFlow(NewsUiState())
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()
}
```

### Compose alternative -- `mutableStateOf`:

```kotlin
class NewsViewModel(private val repository: NewsRepository) : ViewModel() {

    var uiState by mutableStateOf(NewsUiState())
        private set  // only ViewModel can mutate

    fun fetchArticles(category: String) {
        viewModelScope.launch {
            try {
                val items = repository.newsItemsForCategory(category)
                uiState = uiState.copy(newsItems = items)
            } catch (e: IOException) {
                uiState = uiState.copy(userMessages = listOf(Message(e)))
            }
        }
    }
}
```

### Combining multiple streams

When UiState depends on multiple data sources, use `combine` + `stateIn`:

```kotlin
class InterestsViewModel(
    authorsRepository: AuthorsRepository,
    topicsRepository: TopicsRepository,
) : ViewModel() {
    val uiState: StateFlow<InterestsUiState> = combine(
        authorsRepository.getAuthorsStream(),
        topicsRepository.getTopicsStream(),
    ) { authors, topics ->
        InterestsUiState.Success(authors = authors, topics = topics)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = InterestsUiState.Loading,
    )
}
```

### Rules:

- `uiState` is `StateFlow` or Compose `mutableStateOf` -- **not** `LiveData`, not a raw data model
- Use `stateIn(WhileSubscribed(5_000))` when the source is a `Flow` from the data layer
- Expose `MutableStateFlow` as `asStateFlow()` (never expose the mutable reference directly)
- `UiState` is a `data class` with all `val` (or sealed interface for exclusive states)
- Errors go **into** the state -- `userMessages: List<Message>` -- not as separate side-channels
- Use `distinctUntilChanged()` when observing a single-stream UiState to reduce unnecessary
  emissions

### Single stream vs multiple streams

Default: **single stream**. Keeps related state consistent -- no scenario where
one field updates but another doesn't.

Use **multiple streams** only when:

- The data types are completely unrelated and always updated independently
- The `UiState` has so many fields that every minor update causes unnecessary re-renders

```kotlin
// Related states bundled -- bookmark only shows when signed in AND premium
data class NewsUiState(
    val isSignedIn: Boolean = false,
    val isPremium: Boolean = false,
    val newsItems: List<NewsItemUiState> = emptyList(),
    val userMessages: List<Message> = emptyList(),
)

// Derived property -- no logic needed in UI
val NewsUiState.canBookmarkNews: Boolean get() = isSignedIn && isPremium
```

### PagingData -- special case

`PagingData` represents **mutable items** that change over time. It must **not** be
part of an immutable UiState. Expose it as a separate stream:

```kotlin
class NewsViewModel : ViewModel() {
    val pagingDataFlow: Flow<PagingData<NewsItem>> =
        repository.getArticles().cachedIn(viewModelScope)
}
```

### Coroutines in ViewModel -- **Strongly Recommended**

- Use Kotlin flows for receiving data from data/domain layers
- Use `suspend` functions with `viewModelScope.launch` for performing actions
- Never create a custom `CoroutineScope` in a ViewModel

---

## State Production Pipeline

### Initialization

Prefer lazy initialization via `stateIn(WhileSubscribed())`. Avoid launching
async work in the ViewModel `init` block (especially with Compose state, which
causes `IllegalStateException`).

For explicit initialization, make the function idempotent:

```kotlin
class MyViewModel : ViewModel() {
    private var initializeCalled = false

    @MainThread
    fun initialize() {
        if (initializeCalled) return
        initializeCalled = true
        viewModelScope.launch { /* seed the pipeline */ }
    }
}
```

### Background thread state updates

For `MutableStateFlow`, the `update()` function is thread-safe.

For Compose `mutableStateOf`, wrap background writes in `Snapshot.withMutableSnapshot`:

```kotlin
fun rollDice() {
    viewModelScope.launch {
        withContext(defaultDispatcher) {
            Snapshot.withMutableSnapshot {
                _uiState.firstDieValue = Random.nextInt(1, 7)
                _uiState.secondDieValue = Random.nextInt(1, 7)
            }
        }
    }
}
```

---

## State Collection in Compose -- **Strongly Recommended**

```kotlin
@Composable
fun MyScreen(viewModel: MyViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // render from uiState
}
```

Always use `collectAsStateWithLifecycle()` -- **not** `collectAsState()`.
It stops collection when the UI is not visible, avoiding unnecessary work and
potential memory leaks.

### Views lifecycle collection (non-Compose)

```kotlin
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiState.collect { uiState ->
            // update UI elements
        }
    }
}
```

---

## UI State Design

```kotlin
// Option A -- single data class with error messages list (Google's pattern)
data class NewsUiState(
    val newsItems: List<NewsItemUiState> = emptyList(),
    val isLoading: Boolean = false,
    val userMessages: List<Message> = emptyList(),  // errors go in state, not side-channels
)

// Option B -- sealed interface for mutually exclusive states
sealed interface NewsUiState {
    data object Loading : NewsUiState
    data class Success(val articles: List<NewsItemUiState>) : NewsUiState
    data class Error(val message: String) : NewsUiState
}
```

- Always immutable (`val`, never `var`)
- One `UiState` per screen -- not per widget
- Never expose raw data models (Room entities, API DTOs) directly in UI state
- Map to a UI-specific model (`ArticleUi`) at the ViewModel boundary

### UI logic vs business logic

| Type           | Owner                                | Example                                              |
|----------------|--------------------------------------|------------------------------------------------------|
| Business logic | ViewModel (delegates to domain/data) | Save bookmark, validate payment                      |
| UI logic       | Composable or plain state holder     | Scroll position, expand/collapse, show/hide nav rail |

Business logic must be applied **before** UI logic in the state pipeline.

---

## Lifecycle -- **Strongly Recommended**

Do not override `Activity.onResume()`, `onPause()`, etc. for UI-related tasks.
Use Compose lifecycle-aware APIs instead:

```kotlin
// Synchronous work on start/stop
LifecycleStartEffect(key) {
    // runs on start
    onStopOrDispose { /* cleanup */ }
}

// Synchronous work on resume/pause
LifecycleResumeEffect(key) {
    // runs on resume
    onPauseOrDispose { /* cleanup */ }
}

// Asynchronous work tied to lifecycle state
LaunchedEffect(lifecycle) {
    lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
        // collect flows, etc.
    }
}
```

---

## Navigation

Use a **single-activity** architecture -- **Strongly Recommended**.

Use **Navigation 3** (Jetpack) to navigate between screens and handle deep links.
Do not start new Activities for in-app navigation.

---

## PR Review Checklist

### ViewModel

- [ ] ViewModel is annotated `@HiltViewModel` and uses constructor injection
- [ ] ViewModel holds no reference to `Context`, `Activity`, `Fragment`, or `View`
- [ ] `AndroidViewModel` is not used
- [ ] ViewModel is only used at screen level -- not injected into reusable components
- [ ] ViewModel is not passed down to child composables -- only state and event lambdas are passed
- [ ] `uiState` is exposed as `StateFlow` or Compose `mutableStateOf` (not `LiveData`, not mutable)
- [ ] `MutableStateFlow` is `private` -- never exposed directly
- [ ] Coroutines launched with `viewModelScope` -- no custom scope created
- [ ] No events sent from ViewModel to UI via Channel or SharedFlow

### UI State

- [ ] `UiState` named as `[Functionality]UiState` (e.g., `NewsUiState`)
- [ ] `UiState` class uses `val` only -- is fully immutable
- [ ] UI state is a `data class` or sealed type -- not a raw data model or entity
- [ ] No Room entities, API DTOs, or network models in UI state
- [ ] Loading and error conditions are represented in state, not side-channelled
- [ ] One-time effects (messages, navigation) modeled as nullable state fields, consumed via
  callback to ViewModel
- [ ] `PagingData` is NOT inside `UiState` -- exposed as a separate stream

### State Collection

- [ ] State collected with `collectAsStateWithLifecycle()` in Compose -- not `collectAsState()`
- [ ] Views use `repeatOnLifecycle(Lifecycle.State.STARTED)` for collection
- [ ] No business logic inside `@Composable` functions
- [ ] No `StateFlow` / `Flow` collected without lifecycle awareness (no naked `LaunchedEffect`
  collecting forever)
- [ ] `distinctUntilChanged()` used where appropriate to reduce emissions

### State Holders

- [ ] Plain state holder classes used for reusable UI components -- not ViewModels
- [ ] State holder dependencies respect lifetime rules (no shorter-lived dependency on longer-lived
  holder)
- [ ] ViewModel is not passed into plain state holders -- only needed data/lambdas

### Lifecycle

- [ ] No `Activity` lifecycle callbacks (`onResume`, `onPause`) used for UI work
- [ ] `LifecycleStartEffect` / `LifecycleResumeEffect` used instead where applicable
- [ ] Resources registered in lifecycle effects are cleaned up in `onStopOrDispose` /
  `onPauseOrDispose`

---

## Anti-Patterns

| Anti-pattern                                        | Why it's wrong                                               | Fix                                                                          |
|-----------------------------------------------------|--------------------------------------------------------------|------------------------------------------------------------------------------|
| `AndroidViewModel` used                             | Android dep in ViewModel                                     | Use `ViewModel`, move dep to data layer                                      |
| `Activity` passed to ViewModel                      | Lifecycle leak + tight coupling                              | Inject abstraction in data layer                                             |
| `collectAsState()` in Compose                       | Collects even when backgrounded                              | Replace with `collectAsStateWithLifecycle()`                                 |
| Business logic in `@Composable`                     | Untestable, violates SoC                                     | Move to ViewModel or use case                                                |
| ViewModel injected into reusable component          | Wrong scope, lifecycle mismatch                              | Use plain state holder class                                                 |
| ViewModel passed to child composables               | Tight coupling, untestable children                          | Pass only state + event lambdas                                              |
| `MutableStateFlow` exposed publicly                 | UI can mutate state directly                                 | Expose as `asStateFlow()`                                                    |
| `Channel` / `SharedFlow` for ViewModel -> UI events | No delivery guarantee after config change, can lose events   | Model as nullable state field; UI calls `viewModel.eventConsumed()` to clear |
| `LiveData` used for new UI state                    | Outdated; no `WhileSubscribed` support                       | Use `StateFlow`                                                              |
| `PagingData` inside `UiState` data class            | PagingData is mutable; breaks immutability                   | Expose as separate `Flow<PagingData<T>>`                                     |
| Launching async work in ViewModel `init`            | Can cause exceptions with Compose state; object leaking risk | Use `stateIn(WhileSubscribed())` or idempotent `initialize()`                |
