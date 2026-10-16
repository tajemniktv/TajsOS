# TajsOS — Repository Instructions

Applies to AI coding agents working in this repository (including Codex and Gemini).
This file defines **TajsOS-specific** expectations, not general agent behavior or tool policy.

## Product and priorities

- **TajsOS** is an experimental, local-first personal operating system for life, projects, knowledge, execution, and reflection. Build one connected system, not isolated feature silos or a generic task app.
- Design for clarity and low-friction use, including neurodivergent users, without making ADHD the product identity.
- **Android and Desktop JVM are primary targets.** iOS is a scaffold; the server is experimental and the former website is archived. Do not present planned features as shipped.
- Strengthen core architecture and workflows before adding breadth. Follow `ROADMAP.md` for priorities.

## Repository map and sources of truth

| Location | Responsibility |
| --- | --- |
| `shared/` | Domain models, repositories, Room persistence, shared business rules |
| `composeApp/` | Compose Multiplatform UI, shell, navigation, presentation; desktop JVM |
| `androidApp/` | Android entry point and platform integrations |
| `server/` | Experimental Ktor backend / sync work |
| `iosApp/` | iOS scaffold |

- **`README.md`**: current capabilities, setup, dependencies, and validation commands.
- **`ARCHITECTURE.md`**: layering, module boundaries, and UI structure.
- **`DESIGN.md`**: normative design tokens, component and interaction guidance for app/website UI.
- **`CONTRIBUTING.md`**: commit, branch, PR, testing, and licensing conventions.
- **`ROADMAP.md`**: product direction, not a list of implemented features.
- Read current code to establish *what exists* and these documents to understand *what is intended*. If they disagree materially, identify the discrepancy rather than silently treating legacy code or aspirational docs as authoritative for both.

## UI and platform boundaries

- Navigation must remain usable in collapsed-sidebar mode, keep canonical root destinations and tab state consistent, and avoid dead clicks or unreachable routes.
- Follow `DESIGN.md` and existing theme/components: no ad hoc colors, duplicated visual primitives, or hardcoded user-facing strings. Use localization resources.
- Account for mouse, keyboard, focus, touch, accessibility, responsive layouts, and relevant platform-specific behavior when changing interactions.
- Keep website tooling and runtime changes isolated from KMP modules unless the task explicitly spans both; share product identity, not implementation assumptions.

## Validation and maintenance

- Select checks according to the changed surface; commands below use Unix wrapper syntax (use `gradlew.bat` on Windows):
  - Shared/Desktop/server: `./gradlew :shared:jvmTest :composeApp:jvmTest :server:test`
  - Android: `./gradlew :androidApp:lintDebug :androidApp:assembleDebug`
  - Desktop packaging: `./gradlew :composeApp:createDistributable`
  - Coverage, when relevant: `./gradlew koverXmlReport`
- Update the **relevant** canonical docs when behavior, architecture, design conventions, or development commands change; do not duplicate their full contents here.
- Follow `CONTRIBUTING.md` and the authorized task workflow for commits/PRs. Never modify release, licensing, or distribution policy incidentally.
- Apply relevant repository skills if present, but verify their paths and instructions against the current checkout. Do not require a particular IDE, MCP server, or external tool to be installed.
