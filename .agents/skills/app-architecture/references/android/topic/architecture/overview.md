# Guide to App Architecture

Source: https://developer.android.com/topic/architecture

## App Composition

A typical Android app comprises multiple app components: Activities, Services, Content Providers,
and Broadcast Receivers, declared in the app manifest. Modern apps use a single-activity
architecture where one Activity serves as a container for screens built with Jetpack Compose.

### Key Constraints

**Multiple Form Factors:** Apps run on phones, tablets, foldables, ChromeOS devices, car displays,
and XR devices. Configuration changes (rotation, folding) force UI recomposition.

**Resource Constraints:** Mobile devices are resource-constrained. The OS may stop app processes to
allocate resources elsewhere.

**Variable Launch Conditions:** App components can be launched individually and out of order. The OS
or user can destroy components at any time. Don't store application data or state in app components.
Make app components self-contained and independent.

---

## Common Architectural Principles

### 1. Separation of Concerns

The most important principle. Design app architecture with clearly defined responsibilities and
boundaries. Don't write all code in an Activity. An Activity's primary role is to host app UI. The
OS controls its lifecycle, frequently destroying/recreating components.

### 2. Adaptive Layouts

Build apps that gracefully handle configuration changes: device orientation, app window size.
Implement adaptive canonical layouts for optimal UX across form factors.

### 3. Drive UI from Data Models

UI should be driven by persistent data models. Data models represent app data, are independent from
UI elements and other components, and are not tied to UI and app component lifecycle. Benefits:
users don't lose data if Android destroys the app, app works with intermittent network, more robust
and testable architecture.

### 4. Single Source of Truth (SSOT)

When defining a new data type, assign single ownership (SSOT). The SSOT owns data and only it can
modify/mutate it. It exposes data using immutable types. Other types call SSOT functions/events to
modify data. Benefits: centralizes changes, protects data from tampering, makes changes traceable.

In practice: the database is typically the source of truth for application data. In other cases,
ViewModel can be the source of truth.

### 5. Unidirectional Data Flow (UDF)

State flows in one direction (typically parent to child). Events that modify data flow in the
opposite direction. State/data flows from higher-scoped types to lower-scoped types. Events
triggered from lower-scoped types until reaching SSOT. Benefits: data consistency, less error-prone,
easier to debug.

---

## Recommended App Architecture

### Layer Structure

Minimum two layers:

1. **UI Layer** -- Displays application data on screen
2. **Data Layer** -- Contains business logic, exposes application data

Optional third layer:

3. **Domain Layer** -- Simplifies/reuses interactions between UI and data layers

### Modern App Architecture Techniques

- Adaptive and layered architecture
- Unidirectional data flow (UDF) in all layers
- UI layer with state holders to manage UI complexity
- Coroutines and flows
- Dependency injection best practices

### UI Layer

Displays application data on screen. Composed of two types of constructs:

- **UI Elements**: Render data on screen (Jetpack Compose functions)
- **State Holders**: Hold data, expose to UI, handle logic (e.g., ViewModel)

### Data Layer

Contains business logic of the app. Made up of repositories, each containing zero to many data
sources. Repository responsibilities: exposing data, centralizing changes, resolving conflicts
between data sources, abstracting sources, containing business logic.

### Domain Layer (Optional)

Handles complex business logic or simpler business logic reused by multiple ViewModels. Common
classes called use cases or interactors. Each use case has responsibility for a single
functionality.

---

## Managing Dependencies Between Components

### Dependency Injection (DI) -- Recommended

Classes define dependencies without constructing them. At runtime, another class provides these
dependencies. Recommended library: **Hilt**. Benefits: scales code easily, clear patterns for
managing dependencies, compile-time verification, quickly switch between test and production
implementations.

### Service Locator

Alternative to DI. Provides a registry where classes obtain dependencies. Less preferred than DI.

---

## General Best Practices

1. **Don't store data in app components** -- Entry points are short-lived. Avoid designating
   Activities, Services, BroadcastReceivers as data sources.
2. **Reduce dependencies on Android classes** -- Make app components the only classes relying on
   Android framework SDK APIs. Improves testability.
3. **Define clear boundaries of responsibility** -- Don't spread code that loads data across
   multiple classes. Don't mix unrelated responsibilities.
4. **Expose minimal implementation details** -- Don't create shortcuts exposing internal
   implementation.
5. **Focus on unique core** -- Use Jetpack libraries for boilerplate. Focus on what makes app
   unique.
6. **Use canonical layouts and app design patterns** -- Optimize UX on multiple form factors.
7. **Preserve UI state across configuration changes** -- Handle resizing, folding, orientation
   changes.
8. **Design reusable and composable UI components** -- Combine/rearrange for various screen sizes.
9. **Make each part testable in isolation** -- Well-defined APIs facilitate testing.
10. **Types responsible for their concurrency policy** -- Types performing long-running blocking
    work must move computation to right thread. Must be main-safe.
11. **Persist as much relevant and fresh data as possible** -- Handle bad connectivity.

---

## Related Documentation

- [About the UI layer](https://developer.android.com/topic/architecture/ui-layer)
- [UI events](https://developer.android.com/topic/architecture/ui-layer/events)
- [State holders and UI state](https://developer.android.com/topic/architecture/ui-layer/stateholders)
- [State production](https://developer.android.com/topic/architecture/ui-layer/state-production)
- [About the data layer](https://developer.android.com/topic/architecture/data-layer)
- [Offline first](https://developer.android.com/topic/architecture/data-layer/offline-first)
- [Domain layer](https://developer.android.com/topic/architecture/domain-layer)
- [Architecture recommendations](https://developer.android.com/topic/architecture/recommendations)
- [Modularization](https://developer.android.com/topic/modularization)
- [Dependency injection](https://developer.android.com/training/dependency-injection)
- [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
