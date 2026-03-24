# TajsOS Roadmap

This roadmap outlines the past, present, and future direction of TajsOS. TajsOS is a local-first personal operating system for life, projects, thoughts, execution, and insight.

**Current Phase:** Experimental / Alpha
**Primary Targets:** Android, Desktop (JVM)

---

## 🟢 Implemented (Core Foundation)

These features are currently present in the codebase and functional:

- **Local-first Architecture:** Core logic separated into the `:shared` module using KMP. Data persistence powered by Room and `NodeEntity` as the central data model.
- **Unified Entity Model (`NodeEntity`):** Supports tasks, notes, ideas, projects, areas, resources, and LifeOS types (open loops, decisions, protocols, etc.).
- **Compose UI Multiplatform:** Shared UI logic targeting Android and Desktop, following the bespoke "Neural Interface" design language (see `DESIGN.md`).
- **Core Views/Screens:**
  - Dashboard (Command Center)
  - Inbox & Capture
  - Today/Focus Views
  - Tasks, Projects, Areas, Notes, Resources
  - Decisions, Templates, Reviews
- **Operating Modes:** Context-driven UI filtering based on seeded modes like COMMAND, FOCUS, and RECOVERY.
- **Insights & Metrics:** `MainViewModel` exposed insights like weekly summaries, context switching, and LifeOS metrics.
- **Biometric Locking:** Basic preference-based biometric authentication flows.

---

## 🟡 In Progress / Partial Scaffold

These features exist partially in the codebase, are scaffolded out, or require further polish:

- **Server/Backend (`:server` module):** A Ktor backend exists but currently binds locally (`127.0.0.1`) and acts as a foundation for future syncing features.
- **Web & iOS Targets:** The repository has scaffolded module targets (`:iosApp` and Web/JS setups in Gradle files), but these are not the primary focus currently.
- **External Integrations:** Data models exist for calendar integrations (`CalendarProviderEntity`, `CalendarEventEntity`), but UI and sync flows may need refinement.
- **Translations:** UI strings are externalized, but full multi-language support is an ongoing effort.
- **Marketing/Documentation Website:** The `website/` folder contains an Astro site, but its content and integration are evolving.

---

## 🔴 Planned (Future Ideas)

These are aspirational ideas that are not yet fully implemented:

- **Cross-device Syncing:** Fully integrate the `:server` module to handle remote, conflict-free syncing of `NodeEntity` and relations across devices.
- **Advanced Insights Analytics:** More complex analysis of the data using the structured Event Logs and Node Snapshots.
- **Push Notifications & Widget Support:** Native platform integrations for reminding users of tasks/events (WorkManager on Android exists but may need expansion).
- **Public API / Extensibility:** Allowing third-party integrations to read/write nodes into TajsOS.
