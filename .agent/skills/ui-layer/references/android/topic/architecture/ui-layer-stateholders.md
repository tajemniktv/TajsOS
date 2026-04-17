# State Holders and UI State

Source: https://developer.android.com/topic/architecture/ui-layer/stateholders

State holders manage UI state and the logic that produces it, providing separation between UI
presentation code and state production code.

---

## UI State Types

1. **Screen UI state**: What to display on screen. Connected with other app layers, contains app
   data.
2. **UI element state**: Properties intrinsic to UI elements (e.g., `ScaffoldState`,
   `LazyListState`).

---

## Logic Types

**Business logic**: Implementation of product requirements. Usually placed in domain or data layers.
State holder delegates.

**UI logic**: How to display UI state on screen. Navigation, animations, permissions. May involve
Android SDK.

**Critical rule**: Business logic must ALWAYS be applied before UI logic. Applying business logic
after UI logic implies business logic depends on UI logic, which is architecturally incorrect.

---

## Types of State Holders

### Business Logic State Holder (ViewModel)

- Produces UI state through user events and data layer reading
- Retained through Activity recreation
- Manages state for navigation destinations until removed from nav graph
- Unique to its UI, not reusable

```kotlin
@HiltViewModel
class AuthorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authorsRepository: AuthorsRepository,
    newsRepository: NewsRepository
) : ViewModel() {
    val uiState: StateFlow<AuthorScreenUiState> = ...
    fun followAuthor(followed: Boolean) { ... }
}
```

Benefits: survives configuration changes, navigation integration (cached while on back stack,
cleared when popped), Jetpack integration (Hilt, etc.).

### UI Logic State Holder (Plain Class)

- Manages UI element state and UI-specific logic
- Does not survive Activity recreation
- Has references to UI-scoped data sources (Context, Resources, NavController)
- Reusable across multiple UIs

```kotlin
@Stable
class NiaAppState(
    val navController: NavHostController,
    val windowSizeClass: WindowSizeClass
) {
    val shouldShowBottomBar: Boolean
        get() = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact

    val shouldShowNavRail: Boolean
        get() = !shouldShowBottomBar

    val currentDestination: NavDestination?
        @Composable get() = navController.currentBackStackEntryAsState().value?.destination
}
```

Use `remember` / `rememberSaveable` for state persistence across config changes in Compose.

---

## Choosing Between ViewModel and Plain Class

**Use ViewModel when**: need business logic access, state should persist as long as screen is
navigable, state should survive Activity recreation, managing state for navigation destinations.

**Use plain class when**: managing only UI logic, state dependent solely on UI lifecycle, reusable
UI components (search bars, chip groups).

**Core principle**: Produce UI state using state holders closest to where it is consumed.

---

## State Holder Dependencies

State holders can depend on other state holders if dependencies have equal or shorter lifetime.

**Valid**: UI logic -> UI logic, Screen level -> UI logic.

**Invalid**: Screen level -> different screen's holder, UI logic -> Screen level.

### Pass Data, Not State Holders

```kotlin
// BAD -- Don't pass ViewModel to plain state holder
class MyScreenState(private val viewModel: MyScreenViewModel)

// GOOD -- Pass only what's needed
class MyScreenState(
    private val someState: StateFlow<SomeState>,
    private val doSomething: () -> Unit,
    private val scaffoldState: ScaffoldState
)
```

---

## ViewModel Usage Warnings

Don't pass ViewModel instances down to other composable functions:

- Couples composable with ViewModel type
- Makes less reusable and harder to test/preview
- No clear single source of truth
- Multiple composables can call ViewModel functions
- Bugs harder to debug

Follow UDF: pass down state, pass events up.

---

## Related Documentation

- [UI layer](https://developer.android.com/topic/architecture/ui-layer)
- [UI events](https://developer.android.com/topic/architecture/ui-layer/events)
- [State production](https://developer.android.com/topic/architecture/ui-layer/state-production)
- [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel)
