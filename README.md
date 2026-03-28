# TajsOS

## Introduction

**TajsOS** is a local-first personal operating system for life, projects, thoughts, execution, and
insight. It is not necessarily for ADHD brains, but it is designed with neurodivergent brains in
mind.

### Features

- a **command center** for what matters now,
- a **second brain** for notes, ideas, and context,
- a **project/life manager** for keeping different domains organized,
- an **insight layer** for lightweight patterns, reviews, and self-understanding.

---

### Core ideas

The app is built around a unified model where different things in life can still be connected:

- **Inbox captures** for fast intake before forced classification
- **Tasks** for execution
- **Notes** for durable knowledge and reference
- **Records** for chronological reflections, logs, and observations
- **Projects** for outcomes
- **Areas** for ongoing responsibility
- **Relations, schedules, and reminders** as shared layers across the system
- **Focus, review, and tracking data** as read models over shared life objects

Instead of splitting everything into isolated tools, TajsOS tries to make them work as one system.

Built-in domains (for example Finances, Health, Education, Relationships) are first-class lenses
over shared system data, while Areas remain generic user-defined containers.

---

## Current status

TajsOS is in a **highly experimental** stage and the product, architecture, and UX may still change
significantly.

### Current priorities

- Android
- Desktop

Other platforms and integrations may be explored later, but Android/Desktop are the main focus for
now.

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

---

## Repository structure

```text
androidApp/   Android-specific app entrypoint
composeApp/   Shared Compose UI and navigation
shared/       Core data models, entities, repository, business logic
server/       Ktor backend for sync / remote features
website/      Website / documentation
iosApp/       iOS scaffold
