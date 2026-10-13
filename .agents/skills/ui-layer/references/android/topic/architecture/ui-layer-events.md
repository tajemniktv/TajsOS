# UI Events

Source: https://developer.android.com/topic/architecture/ui-layer/events

UI events are actions handled in the UI layer, either by the UI itself or by the ViewModel. The most
common type is user events produced by user interaction.

---

## Logic Types

**Business logic**: What to do with state changes (making payments, storing preferences). Handled by
domain/data layers and ViewModel.

**UI behavior logic**: How to display state changes (navigation, showing messages). Handled by the
UI.

---

## Event Decision Tree

- Event originated in ViewModel -> Update UI state
- Event originated in UI + requires business logic -> Delegate to ViewModel
- Event originated in UI + requires UI behavior logic -> Modify UI element state directly

---

## User Events

### Direct UI Events (No Business Logic)

```kotlin
@Composable
fun LatestNewsScreen(viewModel: LatestNewsViewModel = viewModel()) {
    var expanded by remember { mutableStateOf(false) }
    Button(onClick = { expanded = !expanded }) { /* ... */ }  // UI logic
    Button(onClick = { viewModel.refreshNews() }) { /* ... */ }  // Business logic
}
```

### RecyclerView / LazyColumn Items

Pass business logic as lambda functions in UI state objects, not the ViewModel:

```kotlin
data class NewsItemUiState(
    val title: String,
    val body: String,
    val bookmarked: Boolean = false,
    val onBookmark: () -> Unit
)
```

Don't pass the ViewModel into adapters.

### Naming

ViewModel functions handling user events use verbs: `addBookmark(id)`, `logIn(username, password)`.

---

## ViewModel Events

### Key Principle

UI actions originating from the ViewModel should ALWAYS result in a UI state update. This complies
with UDF, makes events reproducible after configuration changes, and guarantees UI actions won't be
lost.

### Example: Login Navigation

```kotlin
data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isUserLoggedIn: Boolean = false
)
```

### Consuming Events That Trigger State Updates

For transient messages, the UI notifies the ViewModel when consumed:

```kotlin
class LatestNewsViewModel(...) : ViewModel() {
    private val _uiState = MutableStateFlow(LatestNewsUiState(isLoading = true))
    val uiState: StateFlow<LatestNewsUiState> = _uiState

    fun refreshNews() {
        viewModelScope.launch {
            if (!internetConnection()) {
                _uiState.update { it.copy(userMessage = "No Internet connection") }
                return@launch
            }
        }
    }

    fun userMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
```

Compose consumption:

```kotlin
val uiState by viewModel.uiState.collectAsStateWithLifecycle()

uiState.userMessage?.let { userMessage ->
    LaunchedEffect(userMessage) {
        snackbarHostState.showSnackbar(userMessage)
        viewModel.userMessageShown()
    }
}
```

---

## Navigation Events

### User-Initiated (Direct)

```kotlin
Button(onClick = onHelp) { Text("Get help") }  // Caller navigates
```

### Validation-Triggered

ViewModel validates, updates state. UI observes state and navigates:

```kotlin
LaunchedEffect(viewModel, lifecycle) {
    snapshotFlow { viewModel.uiState }
        .filter { it.isUserLoggedIn }
        .flowWithLifecycle(lifecycle)
        .collect { currentOnUserLogIn() }
}
```

---

## Why NOT Channel/SharedFlow for ViewModel Events

When using Kotlin Channels or reactive streams for one-time events, they don't guarantee delivery:

1. Producer outlives consumer
2. "Delivered exactly once" contract impossible to guarantee
3. Users miss critical information
4. Testing difficulties

**Always use UI state updates instead.**

| Aspect                             | UI State                | Channels            |
|------------------------------------|-------------------------|---------------------|
| Delivery guarantee                 | Guaranteed              | Not guaranteed      |
| Replayability after config changes | Yes                     | No                  |
| Testability                        | Easy                    | Difficult           |
| Consistency                        | Faithful representation | Can be inconsistent |

---

## Related Documentation

- [UI layer](https://developer.android.com/topic/architecture/ui-layer)
- [State holders and UI state](https://developer.android.com/topic/architecture/ui-layer/stateholders)
- [ViewModel: One-off event antipatterns](https://medium.com/androiddevelopers/viewmodel-one-off-event-antipatterns-16a1da869b95)
