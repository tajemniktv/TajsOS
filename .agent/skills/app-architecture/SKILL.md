---
name: app-architecture
description: >-
  Overview of Google's recommended Android app architecture: three-layer model
  (UI, Domain, Data), core principles (SoC, adaptive layouts, SSOT, UDF,
  main-safety), dependency injection with Hilt, general best practices, and
  multi-module structure. Use this skill for general architecture questions.
  For layer-specific code review, use the focused layer skills: ui-layer,
  data-layer, domain-layer.
license: Complete terms in LICENSE.txt
metadata:
  author: Google LLC
  keywords:
    - android
    - architecture
    - separation-of-concerns
    - single-source-of-truth
    - unidirectional-data-flow
    - dependency-injection
    - hilt
    - modularization
    - jetpack
---

# Android App Architecture -- Overview

**Source of truth:
** [developer.android.com/topic/architecture](https://developer.android.com/topic/architecture)

## TajsOS Scope Note

This is an Android architecture reference. In TajsOS, use it as technical guidance, but do not treat
it as authoritative for `commonMain` or desktop-specific decisions.

## References

- *[Architecture overview](references/android/topic/architecture/overview.md)* -- full architectural
  principles and best practices
- *[Architecture recommendations](references/android/topic/architecture/recommendations.md)* --
  prioritized recommendations for all layers

> For deeper guidance use the focused layer skills:
> - **UI Layer** (ViewModel, UDF, state, Lifecycle) -> `ui-layer`
> - **Data Layer** (repositories, data sources, models, naming) -> `data-layer`
> - **Domain Layer** (use cases, when to add them) -> `domain-layer`

---

## Three-Layer Architecture & Dependency Rule

```
+-----------------------+
|      UI Layer         |  ViewModels, Compose screens, UiState
+-----------------------+
|  Domain Layer         |  Use cases -- optional
+-----------------------+
|    Data Layer         |  Repositories, Data Sources
+-----------------------+
```

**Upper layers depend on lower layers -- never the reverse.**
UI must never access a data source directly. Only through repositories.

---

## Core Principles

| Principle                   | Rule                                                                                                                                                                                                                                                                                                                   |
|-----------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Separation of concerns      | Each class has one responsibility. Activities/Fragments are UI containers only. Don't write all code in an Activity.                                                                                                                                                                                                   |
| Adaptive layouts            | Build apps that handle configuration changes (rotation, folding, resizing) gracefully. Implement adaptive canonical layouts for optimal UX across form factors.                                                                                                                                                        |
| Drive UI from data models   | UI renders persistent data models -- never computes business values itself. Data models are independent of UI elements and app component lifecycle.                                                                                                                                                                    |
| Single Source of Truth      | One owner per data type; only the SSOT can modify/mutate data. Others read via immutable interface.                                                                                                                                                                                                                    |
| Unidirectional Data Flow    | State flows down; events flow up. Events triggered from lower-scoped types flow until reaching the SSOT where data is modified.                                                                                                                                                                                        |
| Reduce Android dependencies | App components (Activity, Service, etc.) should be the primary classes relying on Android framework SDK APIs. Abstract business logic and data layer classes away from framework deps. (Exception: UI-layer plain state holders may reference Context/Resources since they follow UI lifecycle -- see ui-layer skill.) |
| Main-safety                 | Every public function is safe to call from the main thread. Types doing long-running work are responsible for moving to the right thread.                                                                                                                                                                              |

---

## General Best Practices

These are from the official architecture overview. Apply them alongside the layer-specific rules.

1. **Don't store data in app components** -- Activities, Services, and BroadcastReceivers are
   short-lived entry points. Never designate them as data sources. They should only coordinate to
   retrieve relevant data subsets.
2. **Reduce dependencies on Android classes** -- Only app components should depend on Android
   framework APIs (Context, Toast). Abstract everything else to improve testability and reduce
   coupling.
3. **Define clear boundaries of responsibility** -- Don't spread data-loading logic across multiple
   classes. Don't mix unrelated responsibilities in the same class.
4. **Expose minimal implementation details** -- Don't create shortcuts that expose internal
   implementation. Short-term time gain creates technical debt.
5. **Focus on your app's unique core** -- Don't reinvent the wheel. Use Jetpack libraries for
   repetitive boilerplate. Focus engineering energy on what makes your app unique.
6. **Use canonical layouts and app design patterns** -- Use Jetpack Compose adaptive UI APIs. Review
   the gallery of app design patterns and canonical layouts for multiple form factors.
7. **Preserve UI state across configuration changes** -- Handle display resizing, folding, and
   orientation changes. Architecture must verify user state is maintained seamlessly.
8. **Design reusable and composable UI components** -- Build components that can be
   combined/rearranged for various screen sizes without significant refactoring.
9. **Make each part testable in isolation** -- Well-defined APIs between modules facilitate
   independent testing.
10. **Types are responsible for their concurrency policy** -- If a type performs long-running
    blocking work, it is responsible for moving computation to the right thread. The type must be
    main-safe.
11. **Persist as much relevant and fresh data as possible** -- Not all users have constant
    high-speed connectivity. Offline support improves experience on bad connections.

---

## Dependency Injection -- **Strongly Recommended**

Use **constructor injection** whenever possible.

For the DI framework:

- **Simple apps** (few screens, no WorkManager, no nav back-stack scoping): manual DI is acceptable
- **Complex apps** (multiple screens with ViewModels, WorkManager, nav-scoped ViewModels): use *
  *Hilt**

```kotlin
// Application-scoped -- one instance per app lifetime
@Singleton
class NewsRepository @Inject constructor(...)

// ViewModel-scoped -- one instance per ViewModel
@HiltViewModel
class NewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository,
) : ViewModel()
```

Rules:

- Prefer constructor injection. Manual DI is acceptable for simple apps; avoid service locator in
  complex apps
- `object` singletons with dependencies -> replace with `@Singleton` Hilt bindings
- Use `@InstallIn(SingletonComponent::class)` for data/domain layer modules
- Repository and data source classes should be `internal`; Hilt modules are the public wiring
  boundary

---

## Multi-Module Structure

> **Note:** This is one common pattern (see NowInAndroid). The official docs link to modularization
> guidance but do not prescribe a specific module layout. Adapt to your project's needs.

```
:app                         <- wires everything; depends on all feature modules
:feature:<name>:ui           <- UI layer
:feature:<name>:domain       <- use cases, domain models (zero Android imports)
:feature:<name>:data         <- repository impls, data sources
:core:model                  <- shared data classes
:core:network                <- Retrofit / Ktor setup
:core:database               <- Room setup
:core:testing                <- fakes and test utilities
```

Hard rules (regardless of module layout):

- Domain modules have **zero** `android.*` / `androidx.*` imports -- pure Kotlin
- UI modules do **not** directly depend on data modules
- Shared models live in a core module -- not inside any feature
- Circular dependencies between modules are forbidden
- Everything not needed outside a module is `internal`

---

## App Components Are Entry Points, Not Data Owners

Android app components (Activities, Services, BroadcastReceivers, ContentProviders) are launched
individually and out of order by the OS. The OS can destroy any component at any time. They are
short-lived and you do not control their lifecycle.

**Never store application data or state in app components.** Make components self-contained and
independent. Use the data layer (repositories, local DB) as the source of truth.

---

## General PR Checklist

- [ ] No data or state stored in `Activity`, `Service`, or `BroadcastReceiver`
- [ ] New class has minimum visibility -- `internal` preferred over `public` inside modules
- [ ] Dependency injection used for all cross-layer dependencies -- no `object` with injected deps
- [ ] No Android imports in domain modules
- [ ] UI modules do not directly reference data modules
- [ ] Shared models are in a core module, not inside feature modules
- [ ] App handles configuration changes (rotation, resize) without data loss
- [ ] Each type that does blocking work manages its own concurrency -- caller does not dispatch
- [ ] Offline scenarios considered -- persistent data used where appropriate
