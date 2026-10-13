# UI State Production

Source: https://developer.android.com/topic/architecture/ui-layer/state-production

State production is the incremental application of changes to UI state. The fundamental principle: "
state is; events happen."

---

## Events vs State

| Aspect | Events                                  | State                      |
|--------|-----------------------------------------|----------------------------|
| Nature | Transient, unpredictable, finite period | Always exists              |
| Role   | Inputs of state production              | Output of state production |
| Source | Product of UI or other sources          | Consumed by the UI         |

---

## Pipeline Components

**Inputs** (sources of state change): user events, UI logic APIs, domain/data layer sources.

**State holders**: apply business logic and/or UI logic to produce UI state.

**Output**: the rendered UI state consumed by the application.

---

## State Production APIs

| Pipeline Stage | API                                |
|----------------|------------------------------------|
| Input          | Coroutines, Flows, callbacks       |
| Output         | StateFlow, Compose State, LiveData |

---

## One-Shot APIs

### Using StateFlow

```kotlin
class DiceRollViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DiceUiState())
    val uiState: StateFlow<DiceUiState> = _uiState.asStateFlow()

    fun rollDice() {
        _uiState.update { currentState ->
            currentState.copy(
                firstDieValue = Random.nextInt(from = 1, until = 7),
                secondDieValue = Random.nextInt(from = 1, until = 7),
                numberOfRolls = currentState.numberOfRolls + 1,
            )
        }
    }
}
```

### Using Compose State

```kotlin
@Stable
interface DiceUiState {
    val firstDieValue: Int?
    val secondDieValue: Int?
    val numberOfRolls: Int?
}

private class MutableDiceUiState : DiceUiState {
    override var firstDieValue: Int? by mutableStateOf(null)
    override var secondDieValue: Int? by mutableStateOf(null)
    override var numberOfRolls: Int by mutableStateOf(0)
}

class DiceRollViewModel : ViewModel() {
    private val _uiState = MutableDiceUiState()
    val uiState: DiceUiState = _uiState
}
```

---

## Asynchronous Mutations

```kotlin
private fun createNewTask() {
    viewModelScope.launch {
        try {
            tasksRepository.saveTask(newTask)
            _uiState.update { it.copy(isTaskSaved = true) }
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (exception: Exception) {
            _uiState.update { it.copy(userMessage = getErrorMessage(exception)) }
        }
    }
}
```

Note: Coroutines in `viewModelScope` run to completion. Don't use for operations lasting 5+ seconds;
use WorkManager.

---

## Background Thread State Updates

**StateFlow**: `update()` is thread-safe.

**Compose State**: wrap in `Snapshot.withMutableSnapshot {}`:

```kotlin
withContext(defaultDispatcher) {
    Snapshot.withMutableSnapshot {
        _uiState.firstDieValue = SlowRandom.nextInt(from = 1, until = 7)
        _uiState.secondDieValue = SlowRandom.nextInt(from = 1, until = 7)
    }
}
```

---

## Stream APIs

### Combining Multiple Streams

```kotlin
val uiState = combine(
    authorsRepository.getAuthorsStream(),
    topicsRepository.getTopicsStream(),
) { availableAuthors, availableTopics ->
    InterestsUiState.Interests(authors = availableAuthors, topics = availableTopics)
}.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5_000),
    initialValue = InterestsUiState.Loading
)
```

### SharingStarted Strategies

- `WhileSubscribed()`: pipeline active only when UI is visible
- `Lazily`: pipeline active as long as user may return to UI (backstack)

---

## Combined One-Shot and Stream Sources

```kotlin
val uiState: StateFlow<TaskDetailUiState> = combine(
    _isTaskDeleted,
    _task
) { isTaskDeleted, task ->
    TaskDetailUiState(task = task.data, isTaskDeleted = isTaskDeleted)
}.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5_000),
    initialValue = TaskDetailUiState()
)
```

Use `snapshotFlow { }` to convert Compose State to Flow for combining.

---

## Initialization

### Lazy Initialization (Preferred)

Use `stateIn(WhileSubscribed())` for lifecycle-aware, lazy initialization.

### Explicit Initialization

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

### Avoid

- Launching asynchronous operations in `init` block
- Updating Compose State in ViewModel `init` block (causes `IllegalStateException`)

---

## Output Types by Consumer

| Input             | Consumer | Output                     |
|-------------------|----------|----------------------------|
| One-shot APIs     | Views    | StateFlow or LiveData      |
| One-shot APIs     | Compose  | StateFlow or Compose State |
| Stream APIs       | Views    | StateFlow or LiveData      |
| Stream APIs       | Compose  | StateFlow                  |
| One-shot + Stream | Views    | StateFlow or LiveData      |
| One-shot + Stream | Compose  | StateFlow                  |

---

## Related Documentation

- [UI layer](https://developer.android.com/topic/architecture/ui-layer)
- [State holders and UI state](https://developer.android.com/topic/architecture/ui-layer/stateholders)
- [Architecture samples](https://github.com/android/architecture-samples)
- [Now in Android](https://github.com/android/nowinandroid)
