# TajsOS

## Introduction

**TajsOS** is a local-first personal operating system for life, projects, thoughts, execution, and
insight. It is not necessarily for ADHD brains, but it is designed with neurodivergent brains in
mind.

### Features

- a **command center** for what matters now,
- a **knowledge layer** for notes, records, and reference,
- a **project/life manager** for keeping different domains organized,
- an **insight layer** for lightweight patterns, reviews, and self-understanding.

---

### Core model

The app is built around a small set of shared life objects that can still stay connected:

- **Inbox captures** for fast intake before forced classification
- **Tasks** for execution
- **Notes** for durable knowledge and reference
- **Records** for chronological reflections, logs, and observations
- **Projects** for outcomes
- **Areas** for ongoing responsibility
- **Relations, schedules, and reminders** as shared layers across the system
- **Focus, review, and tracking data** as read models over shared life objects

Instead of splitting everything into isolated tools, TajsOS tries to make them work as one system.

### Local persistence shape

The local model now keeps a compatibility `NodeEntity` spine for the current app shell, while moving
deeper behavior into typed companion tables instead of growing one giant nullable row.

- `InboxEntryEntity` stores raw capture before triage.
- `NodeEntity` remains the shared local identity row for task/note/record/project/area items.
- Typed companion tables (`TaskFacet`, `NoteFacet`, `RecordFacet`, `ProjectFacet`, `AreaFacet`)
  hold object-specific state.
- `RelationEntity`, tags, attachments, and domain assignments stay cross-cutting and first-class.
- `ScheduleEntryEntity` stores time-supporting structure with epoch-based local date support instead
  of expanding string-matched date logic.
- `RichContentDocumentEntity` provides optional long-form/structured bodies without turning the whole
  ontology into generic blocks.
- Saved views are persisted as projections over typed shared objects, not as a spreadsheet engine or
  competing item type.

This keeps the ontology small, typed, and local-first while still leaving room for richer
knowledge, review, and planning surfaces.

---

## Current status

TajsOS is in a **highly experimental** stage and the product, architecture, and UX may still change
significantly.

### Current priorities

- Android
- Desktop

Other platforms and integrations may be explored later, but Android/Desktop are the main focus for
now.

### Desktop mouse controls

Desktop surfaces now use a shared mouse interaction model:

- Left click: primary activation.
- Right click: context menu on major navigable shell/list/card items.
- Middle click: mirrors primary activation on navigable surfaces.
- Mouse back/forward buttons: wired into app navigation.

Current limits:

- Forward navigation can only replay concrete routes (dynamic route patterns with unresolved
  placeholders are intentionally skipped).
- Context menus are currently focused on shared shell/navigation and high-traffic list/card surfaces
  rather than every form control.

---

## Documentation

- See [`ROADMAP.md`](./ROADMAP.md) for the phased roadmap.
- See [`AGENTS.md`](./AGENTS.md) for agent rules and project guardrails.
- See [`CHANGELOG.md`](./CHANGELOG.md) for release notes
- See [`CONTRIBUTING.md`](./CONTRIBUTING.md) for contributing guidelines
- See [`CODE_OF_CONDUCT.md`](./CODE_OF_CONDUCT.md) for code of conduct
- See [`DESIGN.md`](./DESIGN.md) for visual design principles
- See [`LICENSE.md`](./LICENSE.md) for license information

---

## Tech stack

- **Language:** Kotlin
- **App model:** Kotlin Multiplatform
- **UI:** Compose Multiplatform
- **Design system:** Material 3
- **Architecture:** pragmatic layered architecture
- **State management:** ViewModel + StateFlow + immutable UI state
- **Async:** Kotlin coroutines
- **Persistence:** Room + DataStore
- **Backend:** Ktor (for sync / remote features)
- **Build system:** Gradle Kotlin DSL

### UI layering

The Compose app now separates UI composition into three explicit layers:

- `AppShell` for persistent chrome such as the sidebar, top header, shell spacing, and route host.
- `ScreenScaffold` / `SplitScreenScaffold` for reusable page-level structure, width policy, and scroll behavior.
- Screen route/content composables for state collection and screen-specific rendering only.

---

## Repository structure

```text
androidApp/   Android-specific app entrypoint
composeApp/   Shared Compose UI and navigation
shared/       Core data models, entities, repository, business logic
server/       Ktor backend for sync / remote features
website/      Website / documentation
iosApp/       iOS scaffold
```

---

## Project stats

[![CI](https://img.shields.io/github/actions/workflow/status/tajemniktv/TajsOS/ci.yml?branch=main&label=CI)](https://github.com/tajemniktv/TajsOS/actions/workflows/ci.yml)
[![Codecov](https://codecov.io/gh/tajemniktv/TajsOS/branch/main/graph/badge.svg)](https://codecov.io/gh/tajemniktv/TajsOS)
[![CodeQL](https://img.shields.io/github/actions/workflow/status/tajemniktv/TajsOS/codeql.yml?branch=main&label=CodeQL)](https://github.com/tajemniktv/TajsOS/actions/workflows/codeql.yml)
[![Latest release](https://img.shields.io/github/v/release/tajemniktv/TajsOS?display_name=release)](https://github.com/tajemniktv/TajsOS/releases)
[![Last commit](https://img.shields.io/github/last-commit/tajemniktv/TajsOS)](https://github.com/tajemniktv/TajsOS/commits/main)
[![Open issues](https://img.shields.io/github/issues/tajemniktv/TajsOS)](https://github.com/tajemniktv/TajsOS/issues)
[![Open PRs](https://img.shields.io/github/issues-pr/tajemniktv/TajsOS)](https://github.com/tajemniktv/TajsOS/pulls)

<p align="center">
  <img src="https://github-readme-stats.vercel.app/api?username=tajemniktv&repo=TajsOS&show_icons=true&hide_border=true&rank_icon=github" alt="TajsOS repository stats" />
</p>

<p align="center">
  <img src="https://github-readme-stats.vercel.app/api/top-langs/?username=tajemniktv&repo=TajsOS&layout=compact&hide_border=true" alt="TajsOS top languages" />
</p>
