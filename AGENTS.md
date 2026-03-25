# Guidelines for AI Agents

This is a guidelines file for agents working on TajsOS.

## Project identity

TajsOS is a low-friction "Second Brain" multiplatform app for managing your life. It is not
neccessarily for ADHD brains, but it is designed with ADHD brains in mind. It tries to replace
overwhelming lists with a mechanical, satisfying control center that makes task capture and
execution feel like operating heavy machinery.

## Maintaining AGENTS.md Files

When updating AGENTS.md files, follow these principles:

- **No hardcoded counts** — Don't write "5 modules"; these become outdated instantly
- **Document constraints, not descriptions** — Focus on non-obvious behaviors, gotchas, and
  cross-crate dependencies
- **Verify before documenting** — Grep/read the code to confirm claims are accurate
- **Delete outdated info** — Outdated docs are worse than no docs

## Commit Message Convention

```
<type>: <description>

[optional body]
```

### Types

| Type       | Description                           |
|------------|---------------------------------------|
| `feat`     | New feature                           |
| `fix`      | Bug fix                               |
| `refactor` | Code refactoring (no behavior change) |
| `docs`     | Documentation only                    |
| `test`     | Adding or updating tests              |
| `chore`    | Maintenance tasks                     |
| `perf`     | Performance improvements              |

### Versioning Conventions

| Bump Type | When to Use                                            | Example           |
|-----------|--------------------------------------------------------|-------------------|
| `patch`   | Bug fixes, small features, additive parser support     | `1.2.0` → `1.2.1` |
| `minor`   | New client support, significant features, UI overhauls | `1.1.2` → `1.2.0` |
| `major`   | Breaking changes (never used so far)                   | `1.2.1` → `2.0.0` |

## Tech stack

- **Targets:** Android + Desktop (JVM) + web (iOS and web targets scaffolded, Android/Desktop
  primary)
- **Language:** Kotlin
- **App model:** Kotlin Multiplatform (KMP)
- **UI:** Compose Multiplatform (CMP, shared UI)
- **Design system:** Material 3
- **Architecture:** Pragmatic layered architecture with shared `data`/`domain` (in `:shared`) and UI
  separated from business logic. Core logic and data models live in
  `shared/src/commonMain/kotlin/com/tajemniktv/tajsos/data/`.
- **Module structure:**
    - `:shared` — core logic, Room entities, repositories, and data models
    - `:composeApp` — shared Compose UI, navigation, and screens for all platforms
    - `:androidApp` — Android-specific entry point
    - `:server` — Ktor-based backend for sync/remote features
- **Calendar integration:** Supports external calendar providers and events (Google, Outlook, ICS,
  etc.)
- **Localization:** The app is being developed with localization in mind; UI strings are sourced for
  translation.
- **State management:** ViewModel + StateFlow + immutable UI state (see `MainViewModel` in
  ComposeApp)
- **Async:** Kotlin coroutines
- **Navigation:** State-driven Compose navigation using a sealed `Screen` class; each major feature
  has a dedicated screen in `composeApp/src/commonMain/kotlin/com/tajemniktv/tajsos/ui/screens/`.
- **Persistence:** Room for structured data, DataStore for preferences/settings
- **Sync:** Local-first approach with a remote backend for cross-device sync
- **Background work:** WorkManager on Android only when it provides clear value
- **Dependency injection:** Manual DI at first, Hilt only if complexity justifies it
- **Build system:** Gradle with Kotlin DSL and version catalogs (`gradle/libs.versions.toml`). All
  modules use plugin aliases and share dependency versions.

---

## Agent behavior expectations

When working in this repo, agents should:

1. Preserve existing behavior unless asked to change it.
2. Explain tradeoffs clearly when making structure decisions.
3. Prefer editing existing files over creating unnecessary new ones.
4. Keep diffs focused and reviewable.
5. Update docs when the product scope or setup changes.
6. Avoid broad rewrites unless explicitly requested.
7. If available, use MCP servers or tools to check code health (eg. CodeScene, SonarQube)

---

## Documentation expectations

When making meaningful changes, keep these current:

- `README.md` for product and setup
- `AGENTS.md` for agent behavior and project rules
- `ROADMAP.md` for phased direction when scope evolves
- `CHANGELOG.md` for release notes
- `CONTRIBUTING.md` for contributing guidelines
- `CODE_OF_CONDUCT.md` for code of conduct
- `DESIGN.md` for visual design principles
- `ARCHITECTURE.md` for understanding the application structural design and boundaries

---

## Additional project conventions and patterns

- **Data model conventions:**
    - The main entity is `NodeEntity` (unified model for `task`, `note`, `idea`, `project`, `area`,
      `resource`, and LifeOS types like `open_loop`, `decision`, `maintenance`, `protocol`,
      `person`,
      etc.).
    - Operating Modes are implemented via `ModeEntity`, `ModePreferenceEntity`,
      `ModeAreaFilterEntity`, `ModeTypeFilterEntity`, and `ModeUsageLogEntity` for context-driven
      UI,
      filtering, and user workflows. Modes like "COMMAND", "FOCUS", and "RECOVERY" are seeded and
      used
      throughout the app.
    - Relations are handled via `RelationEntity` for linking nodes (e.g., tasks to projects).
    - "Today" is implemented as `TodayPinEntity` (table for daily pinning).
    - Focus sessions and daily tracking: `FocusSessionEntity`, `TrackEntryEntity`.
    - Templates: `TemplateEntity` for reusable item structures.
    - Reviews: `ReviewEntity` for formal reflection sessions.
    - Snapshots: `NodeSnapshotEntity` for versioning node content.
    - Attachments: `AttachmentEntity` for files/links associated with nodes.
    - Calendar: `CalendarProviderEntity` and `CalendarEventEntity` for external calendar
      integration.
- **Repository pattern:**
    - All data access is funneled through `AppRepository` in `shared`.
    - ViewModels (e.g., `MainViewModel`) expose StateFlows for UI state.
    - AppRepository also provides flows for calendar, template, review, snapshot, and attachment
      data.
- **Status/type conventions:**
    - `NodeEntity` uses `type` (`task`, `note`, `project`, `area`, etc.) and `status` (`active`,
      `done`, `archived`, `on_hold`, `someday`, `blocked`).
    - `ReviewEntity` uses `type` (`daily`, `weekly`, `monthly`).
    - `TemplateEntity` uses `nodeType` (`task`, `note`, `project`).
- **Main entrypoints:**
    - UI: `App.kt` in `composeApp/src/commonMain/kotlin/com/tajemniktv/tajsos/`
    - Data: `AppRepository` and entities in
      `shared/src/commonMain/kotlin/com/tajemniktv/tajsos/data/`
    - Insights/stats: `MainViewModel` exposes a rich `insights` StateFlow for weekly summaries,
      context switching, backlog pressure, and LifeOS-specific metrics (e.g., mood/focus
      correlations,
      area health, context stability, project entropy, and other context-driven insights).
    - Advanced search/filtering: `MainViewModel` supports multi-criteria search (by tag, type,
      status,
      project, area, energy, friction, etc.).
    - Biometric/locking: App supports biometric authentication and locking via preferences and
      ViewModel state.
    - Export: Data export to JSON is available via ViewModel.
