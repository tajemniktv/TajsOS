# TajsOS

## Introduction

**TajsOS** is a local-first personal operating system for life, projects, thoughts, execution, and
insight. It is not necessarily for ADHD brains, but it is designed with neurodivergent brains in
mind.

### Application identity

The application ID and source package root are `com.tajemniktv.tajos`.
Android treats this as a separate app from the previous ID; existing Android app data is not
automatically transferred. Desktop data remains in the existing TajsOS data directory.
Firebase builds require a `google-services.json` downloaded for the new Android registration,
including the `GOOGLE_SERVICES_JSON` repository secret when configured. Do not relabel the old
Firebase client configuration. The public CI placeholder is only for build checks.

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
- The desktop header search entry opens global search by click or keyboard activation;
  compact layouts expose the same destination through a search icon.

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

### Dependency updates and local validation

Gradle dependencies are centralized in `gradle/libs.versions.toml`, and the wrapper is pinned with
a distribution SHA-256 checksum. Prefer the latest stable toolchain and persistence releases;
release candidates are acceptable when compatible with the selected Kotlin compiler and runtime.
Material 3 remains on the Compose 1.12-compatible alpha line rather than pulling the Compose 1.13
alpha runtime into an otherwise stable UI stack.

Firebase SDK versions come from the same BOM in both Android consumers (`androidApp` and the
Android source set of `composeApp`). Do not add independent SDK pins that can drift from the BOM.
Haze blur uses the Haze 2 `haze-blur` and `haze-blur-materials` modules, immutable styles, and explicit
source inputs; disabling shell glass bypasses its source capture and blur modifiers.

Validate shared logic, desktop UI logic, server behavior, Android lint, and Android packaging with:

```powershell
.\gradlew.bat :shared:jvmTest :composeApp:jvmTest :server:test :androidApp:lintDebug :androidApp:assembleDebug :koverXmlReport
.\gradlew.bat :composeApp:createDistributable
```

Website dependencies and their lockfile are maintained separately. Run `npm ci`, `npm run check`,
and `npm run build` from `website/` after updates. See its README for compatibility constraints.
These checks do not establish device acceptance, visual correctness, or iOS compatibility.

Gradle and Kotlin compiler daemons retain their build heaps between invocations. Their idle
timeouts are capped at five minutes in `gradle.properties`, while keeping the heap capacity needed
for KMP compilation. A build after that idle period pays the daemon startup cost again.
Run `.\gradlew.bat --stop` to release the current Gradle version's daemon immediately; its Kotlin
compiler daemon also exits once its clients disconnect. After a toolchain update, older versions'
daemons may remain until their original idle timeout or a stop command using that Gradle version.

### Database backups and aggregate writes

Settings → Data management opens a JSON preview after either export action. **Copy JSON** copies the
complete payload, including content beyond the preview limit; exports are never copied automatically.
Import feedback reports restored entities or format/schema errors. Transfer controls prevent
overlapping actions, retain import text on failure, and release export text when the dialog closes.
A new export clears any prior preview before reading data, so a failed replacement cannot offer an
older snapshot. Node-only exports await a fresh repository result instead of using an initializing UI
cache. Android marks copied backup text sensitive and uses a localized label instead of JSON in the
clip description. This hides the system preview; other apps may still receive text when pasted.
Version checks decode only top-level metadata and skip unknown data without retaining a JSON tree.
Supported payloads still undergo strict decoding. Unsupported bundle and node-only versions fail
before writes. The pasted string and decoded entities still consume memory; imports are not streamed.

Full JSON backups use the versioned `ExportBundle` contract. Schema 4 snapshots every Room table
inside one transaction, including typed facets, schedules, inbox captures, tag assignments, pins,
saved-view children, modes, profile data, attachment references, and history. Snapshot suspend
queries use the transaction connection instead of combining independently observed UI flows.
New exports always include the explicit schema version even with default JSON encoding. Historical
unversioned payloads remain ambiguous; this does not invent a migration or recover omitted state.

Restoring a full backup atomically merges rows by persisted identity and replaces owned companion
state for imported objects; roots absent from the file remain untouched. IDs are not remapped, so
imports are intended for recovery of the same data set, not combining unrelated databases.
Older bundle schemas and node-only exports rebuild node-derived typed state without inventing
creation activity. Unsupported schema versions fail before writes. Fields absent from an old
backup cannot be recovered by hydration.

This is a **database backup**, not a portable archive of external files: attachment paths/URLs are
preserved, but file bytes, Android URI permissions, device-local DataStore settings, and ownership
entitlements are not embedded. Enabled pack keys are informational metadata. Calendar access and
refresh tokens are stripped from snapshots and never accepted from imported payloads, including
historical bundles. Sources with token columns are exported disabled. Every newly imported or
identity-changed source is disabled until locally enabled, including credential-free feeds. An empty
destination needs authorization configured again when required. Restoring into the same profile
preserves existing local tokens and enablement when provider ID, type, account and URL match,
including matching token-free feeds. A credential-bearing identity
collision rejects the entire restore instead of erasing or redirecting local authorization.
Export never changes the live source. Private profile data and calendar feed URLs remain in the
backup, including URLs that themselves grant access to private calendars. This is a private recovery
file, not a sanitized sharing archive. Exports are unencrypted; keep the files private.

The intentional pre-alpha migration policy remains in effect: platform database builders allow
destructive fallback when no migration exists. A database backup does not establish a supported
historical migration chain or make upgrades safe for irreplaceable data. Room schema 34 adds a
non-unique `(nodeId, date)` index for Today pin lookup and repair; older profiles may be reset by the
existing destructive fallback. No migration is provided. The JSON backup contract remains schema 4.

Production repository construction from `AppDatabase` wires a shared Room writer transaction for
aggregate commands, including node side effects, saved-view replacement, triage, and imports.
Network calendar fetching happens before the transaction. Only successful results replace cached
events and advance `lastSyncedAt`; unavailable sources leave the local cache and timestamp intact.
A successful empty calendar intentionally clears its previous events.

Triage retries return the capture's existing target ID, even if that target was later deleted;
dismissed captures cannot be triaged and stale dismissal cannot erase a processed link. Today pin
commands collapse duplicates for the same node and local date without dropping other dates.
Missing/deleted nodes cannot acquire pins, and node deletion removes its pins atomically.
Lifecycle callbacks reload the current node by ID and commit completion with its recurring copy in
one transaction. Repeating the current status does nothing; archiving retains completion history.
An archived node must explicitly reopen before completion; stale completion callbacks cannot create
another recurring occurrence. Unknown lifecycle keys are rejected before writes.
Explicit reopening clears completion metadata and permits a new completion transition. Existing
fixed-duration recurrence rules remain.

Unrelated node edits preserve schedule rows, including multiple reminders and schedule-only detail.
Legacy time edits change only the row matching the previous scalar instant, preserving its identity,
metadata, explicit timezone, and end duration. Other same-kind entries remain independent. Legacy
recurrence edits affect only scalar-backed entries. Full-entity node update APIs still require fresh
caller state; the lifecycle callback is field-scoped, not a general stale-write merge protocol.
Repeated IDs in a batch retain their last value and reconcile side effects once. Recurrence-only
edits retain stored timezone metadata without resolving it; actual timestamp edits still validate
an explicit timezone and roll back the aggregate if that timezone cannot be resolved.

Regression tests use real in-memory Room databases for backup round trips and induced write
failures. DAO-only fake repositories cannot claim to produce complete database backups.

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

![TajsOS repository stats](https://github-readme-stats.vercel.app/api?username=tajemniktv&repo=TajsOS&show_icons=true&hide_border=true&rank_icon=github)

![TajsOS top languages](https://github-readme-stats.vercel.app/api/top-langs/?username=tajemniktv&repo=TajsOS&layout=compact&hide_border=true)
