# TajsOS Architecture

This document explains the intended structural boundaries of TajsOS at a practical level.
It is not meant to fossilize the project. It is meant to keep the app understandable while it evolves.

## Core architectural stance

TajsOS is:
- local-first,
- multiplatform,
- opinionated,
- modular enough to preserve boundaries,
- pragmatic rather than framework-worshipping.

The architecture should help the app feel like one system.
It should not become an abstraction museum.

## Primary goals

The architecture should make it easier to:
- evolve the product without chaos,
- preserve shared behavior across platforms,
- keep business logic out of presentation code,
- keep UI patterns consistent,
- support Android and Desktop as first-class targets,
- add future surfaces without rewriting the whole app.

## Repository shape

At a high level, the repo currently centers around:

- `shared/`  
  Core data models, entities, repositories, and business/domain logic.

- `composeApp/`  
  Shared Compose UI, navigation, screen structure, and presentation logic.

- `androidApp/`  
  Android entry point and platform-specific wiring.

- `server/`  
  Ktor-based remote/sync-oriented backend work.

- `iosApp/`  
  iOS scaffold.

- `website/`  
  Website and documentation surface.

## Layering intent

### 1. Data / persistence layer
Responsible for:
- entities,
- database access,
- local persistence shape,
- repository implementations,
- serialization/storage details,
- platform-agnostic persistence logic where possible.

This layer should not know about screen composition details.

### 2. Domain / application logic
Responsible for:
- use-case-like operations,
- business rules,
- object relationships,
- transformations between raw storage and app behavior,
- consistency rules.

This layer should not depend on visual presentation.

### 3. Presentation layer
Responsible for:
- navigation,
- screen state,
- UI orchestration,
- layout scaffolding,
- user interaction handling,
- platform-aware presentation decisions.

This layer may call into domain/data logic, but should avoid becoming the place where the product rules secretly live.

## UI structure intent

The UI is moving toward a clearer three-part structure:

### AppShell
Owns:
- persistent chrome,
- shell-level responsive behavior,
- route host placement,
- shell overlays,
- global header/sidebar behavior.

### ScreenScaffold family
Owns:
- per-screen page structure,
- titles and header slots,
- content insets,
- width constraints,
- scrolling rules,
- optional multi-pane layouts.

### Screen content / route composables
Own:
- state gathering,
- feature-specific rendering,
- small local interactions,
- wiring blocks together.

They should not all reinvent layout, spacing, header behavior, and shell integration from scratch.

## Product model direction

TajsOS is trying to stay built around a small set of connected life objects rather than a million disconnected feature silos.

Core objects include ideas like:
- inbox captures,
- tasks,
- notes,
- records,
- projects,
- areas,

with cross-cutting layers such as:
- relations,
- reminders,
- schedules,
- review/tracking surfaces.

The architecture should reinforce this shared ontology rather than encourage every new feature to invent its own standalone mini-database and UI universe.

## Architectural constraints

### Prefer explicitness over magic
Typed structures and clear boundaries beat hidden behavior.

### Prefer evolution over rewrite
Improve the current system in-place when possible.
Do not blow up stable foundations for aesthetic reasons.

### Prefer shared primitives over duplicated flows
If multiple screens keep reinventing the same structural pattern, extract the pattern.

### Avoid speculative abstraction
Do not introduce layers just because “large apps usually have them.”

### Keep platform-specific code where it belongs
Platform glue belongs in platform modules, not leaked everywhere.

## What good architectural change looks like

A strong architectural PR usually:
- removes duplication,
- clarifies boundaries,
- improves naming,
- reduces accidental complexity,
- preserves or improves behavior,
- makes future work easier without making current work harder.

## What bad architectural change looks like

A weak architectural PR usually:
- adds indirection without payoff,
- renames everything while solving nothing,
- introduces generic helpers nobody can reason about,
- mixes UI, domain, and persistence concerns more than before,
- optimizes for theoretical scale while hurting current clarity.

## Documentation rule

When architecture changes materially, update:
- `README.md` if the high-level story changed,
- `AGENTS.md` if contributor/agent guardrails changed,
- this file if boundaries or structural intent changed,
- `ROADMAP.md` if priorities shifted.

## Final principle

TajsOS should feel engineered, not accidental.

The architecture is successful when it gives the project room to become more ambitious without becoming more chaotic.
