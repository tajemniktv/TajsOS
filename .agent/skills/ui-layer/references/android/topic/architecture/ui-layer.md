# UI Layer

Source: https://developer.android.com/topic/architecture/ui-layer

The UI layer displays application data and serves as the primary user interaction point. It converts
application data changes into a form the UI can present. The UI is a visual representation of the
application state retrieved from the data layer.

---

## UI State

UI state is the immutable snapshot of information needed for the UI to render fully. It must be
immutable: provides guarantees regarding application state at an instant in time, prevents multiple
sources of truth, enables safe concurrent access.

### Naming Convention

Pattern: `[functionality] + UiState`. Examples: `NewsUiState`, `NewsItemUiState`, `SignInUiState`.

### Example

```kotlin
data class NewsUiState(
    val isSignedIn: Boolean = false,
    val isPremium: Boolean = false,
    val newsItems: List<NewsItemUiState> = listOf(),
    val userMessages: List<Message> = listOf()
)
```

---

## Unidirectional Data Flow (UDF)

1. ViewModel holds and exposes state to UI
2. UI notifies ViewModel of user events
3. ViewModel handles actions and updates state
4. Updated state flows to UI for rendering
5. Repeat for any state-mutating event

Benefits: data consistency, testability, maintainability.

---

## State Holders

Classes responsible for producing UI state and containing the logic for that task.

**ViewModel** -- for screen-level UI state management with data layer access. Survives configuration
changes.

**Plain class** -- for simpler UI elements. Can take Android SDK dependencies (Context, Resources)
since they follow UI lifecycle.

### Logic Types

**Business logic**: Implementation of product requirements (bookmarking, sign-in). Lives in
domain/data layers, never in UI layer.

**UI logic**: How to display state changes (showing/hiding views, navigation, toasts). Lives in the
UI. If complex, create a simple state holder class.

---

## Exposing UI State

### Observable Data Holders

Expose state in StateFlow, LiveData, or Compose State APIs.

**StateFlow pattern:**

```kotlin
class NewsViewModel(...) : ViewModel() {
    private val _uiState = MutableStateFlow(NewsUiState())
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()
}
```

**Compose pattern:**

```kotlin
class NewsViewModel(...) : ViewModel() {
    var uiState by mutableStateOf(NewsUiState())
        private set
}
```

### Single Stream vs Multiple Streams

**Single stream** (default): fewer inconsistencies, easier to understand. Related state should be
bundled.

**Multiple streams**: when data types are completely unrelated, or UiState has many fields causing
unnecessary re-renders. Use `distinctUntilChanged()` to reduce emissions.

### Derived Properties

```kotlin
data class NewsUiState(
    val isSignedIn: Boolean = false,
    val isPremium: Boolean = false,
)
val NewsUiState.canBookmarkNews: Boolean get() = isSignedIn && isPremium
```

---

## Consuming UI State

### Compose

```kotlin
@Composable
fun LatestNewsScreen(viewModel: NewsViewModel = viewModel()) {
    // Automatic lifecycle handling through composition
}
```

### Views

```kotlin
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiState.collect { /* update UI */ }
    }
}
```

Lifecycle-aware collection prevents unnecessary work, memory leaks, and battery drain.

---

## Error Handling in UI State

Model errors as data in state:

```kotlin
data class NewsUiState(
    val userMessages: List<Message> = listOf(),
    val isFetchingArticles: Boolean = false,
    val newsItems: List<NewsItemUiState> = listOf()
)
```

---

## PagingData

`PagingData` represents mutable items. It should NOT be part of immutable UiState. Expose
separately:

```kotlin
val pagingDataFlow: Flow<PagingData<NewsItem>> =
    repository.getArticles().cachedIn(viewModelScope)
```

---

## Related Documentation

- [UI events](https://developer.android.com/topic/architecture/ui-layer/events)
- [State holders and UI state](https://developer.android.com/topic/architecture/ui-layer/stateholders)
- [State production](https://developer.android.com/topic/architecture/ui-layer/state-production)
- [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel)
- [Jetpack Compose state](https://developer.android.com/jetpack/compose/state)
