---
title: Local-First Architecture
description: Data persistence and structural boundaries.
---

## Core Architectural Stance

TajsOS is:

- Local-first
- Multiplatform
- Opinionated
- Modular enough to preserve boundaries
- Pragmatic rather than framework-worshipping

The architecture should help the app feel like one system without becoming an abstraction museum.

## Local Persistence Shape

The local model keeps a compatibility `NodeEntity` spine for the current app shell, while moving deeper behavior into typed companion tables.

- `InboxEntryEntity` stores raw capture before triage.
- `NodeEntity` remains the shared local identity row for task/note/record/project/area items.
- Typed companion tables (`TaskFacet`, `NoteFacet`, `RecordFacet`, `ProjectFacet`, `AreaFacet`) hold object-specific state.
- `RelationEntity`, tags, attachments, and domain assignments stay cross-cutting and first-class.
- `ScheduleEntryEntity` stores time-supporting structure with epoch-based local date support.
- `RichContentDocumentEntity` provides optional long-form/structured bodies.

Saved views are persisted as projections over typed shared objects.
