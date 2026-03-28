# Guidelines for AI Agents

This file defines working constraints for agents contributing to TajsOS.

## Project identity

**TajsOS** is a local-first personal operating system for life, projects, thoughts, execution, and
insight.
The product should feel like one coherent system with multiple lenses over shared life data, not a
collection of disconnected feature silos.

## Maintaining AGENTS.md or docs files

- Avoid hardcoded counts or brittle inventories.
- Document behavioral constraints and architectural boundaries.
- Verify claims in code before writing them.
- Remove outdated guidance instead of preserving stale history.

## Commit message convention

```text
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

### Versioning conventions

| Bump Type | When to Use                                        | Example            |
|-----------|----------------------------------------------------|--------------------|
| `patch`   | Bug fixes, small features, additive support        | `1.2.0` -> `1.2.1` |
| `minor`   | Significant features, client support, UI overhauls | `1.1.2` -> `1.2.0` |
| `major`   | Breaking changes                                   | `1.2.1` -> `2.0.0` |

## Architecture

### Platforms and modules

- Active Gradle modules: `:androidApp`, `:composeApp`, `:shared`, `:server`.
- Active runtime targets in code: Android app, Desktop JVM app, and iOS framework integration.
- `website/` is separate from KMP runtime concerns.
- Server is currently Ktor/Netty and intentionally lightweight.

### Foundations to preserve unless explicitly changing

- KMP + Compose Multiplatform with shared business logic.
- Manual DI (`SharedModule`).
- Coroutines + `StateFlow` reactive state model.
- Room + DataStore persistence split.
- Persisted operating modes and mode preferences.
- Android integrations (share intents, biometrics, voice capture).
- Separate website and server modules.

## Architectural guardrails

### System shape

- Prioritize backend/domain coherence before broad UI surface expansion.
- Design screens as projections (lenses) over shared state, not isolated feature kingdoms.
- Preserve local-first behavior as a non-negotiable baseline.

### ViewModel boundaries

- `MainViewModel` is shell-level orchestration, not the universal domain engine.
- Keep global concerns in root scope (app lifecycle, mode shell, navigation shell, sync status, pack
  availability, global capture/search entry).
- Move feature/domain-heavy orchestration into feature-scoped components.
- Avoid adding new cross-domain branching logic to `MainViewModel` when a bounded feature component
  can own it.

### Domain modeling boundaries

- Treat `NodeEntity` as an overloaded legacy surface that should not absorb unlimited new nullable
  fields.
- Prefer typed companion models/tables for deeper domain behavior.
- Keep relation graph behavior (`RelationEntity`) as a first-class capability.
- Preserve backward compatibility when evolving data shape.

### Type safety boundaries

- Do not introduce new raw string literals for domain state when typed models are feasible.
- Prefer enums, sealed hierarchies, value objects, or centralized constants with strict mapping.
- Treat new string literals as schema-affecting changes.

### Date/time boundaries

- `YYYY-MM-DD` string matching for “today” is a temporary compromise, not a long-term pattern.
- New date-sensitive behavior should use real date abstractions (`LocalDate`/`epochDay`) with
  explicit timezone semantics.

### Sync boundaries

- Keep sync behind abstractions/interfaces in client architecture.
- Current `/sync` behavior is a development stub (in-memory, non-durable) and must not define
  long-term product assumptions.
- Do not couple feature correctness to current stub conflict semantics.

### Data safety boundaries

- `fallbackToDestructiveMigration(true)` indicates pre-alpha schema safety posture.
- Any schema growth should be treated as high-risk and documented clearly.
- Prefer migration-safe evolution and backup/export resilience as architecture matures.

### Pack and shell boundaries

- Pack gating is valid for advanced/specialized capabilities.
- Core app identity and shell structure should remain cohesive and broadly available.
- Avoid making core navigation feel like fragmented DLC partitions.

### Bootstrap boundaries

- Keep system defaults, onboarding examples, and dev/demo data conceptually separate.
- Startup behavior should be explicit and predictable.

## Product lens framing

Feature work should reinforce cohesive read models:

- **Now**: urgent execution state across tasks/events/open loops/mode.
- **Plan**: calendar commitments and forward pressure.
- **Operate**: maintenance, logistics, and routines.
- **Knowledge**: notes, references, and linked entities.
- **Review**: trends, outcomes, unfinished loops, reflection.

## Persistence constraints

- Room is used cross-platform through KMP Room setup.
- Database currently uses `fallbackToDestructiveMigration(true)` on Android and JVM.
- DataStore (`PreferencesRepository`) currently carries biometric settings, active mode, and pack
  ownership/enabling.

## Agent behavior expectations

When working in this repo, agents should:

1. Preserve behavior unless explicitly asked to change it.
2. Keep diffs focused and reviewable.
3. Prefer editing existing files over broad rewrites.
4. Explain architectural tradeoffs when changing structure.
5. Update documentation when constraints or behavior change.
6. Respect typed-domain direction and avoid unnecessary string-state sprawl.
7. Keep sync assumptions abstract/local-first.
8. Add or update KDoc after changes to the codebase.
9. Validate assumptions from current code, not old docs.
10. After a successful build/test, commit changes.
11. When modifying or creating visual aspects of the app, consult DESIGN.md

## Documentation touchpoints

Before broad changes, check and update if impacted:

- `README.md`
- `AGENTS.md`
- `DESIGN.md`
- `LICENSE.md`

If additional docs are added later (for example `ROADMAP.md` or `CHANGELOG.md`), keep references
synchronized.

## High-risk gotchas

- Repository methods include side effects (event logging, relation synchronization, decision
  conversions); prefer repository APIs over direct DAO bypass.
- `MainViewModel` is already large; default to feature-scoped ownership for new domain logic.
- `NodeEntity` growth is architectural debt unless deliberately justified.
- Server sync state is currently in-memory and non-durable.
