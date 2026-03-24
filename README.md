# TajsOS

**TajsOS** is a local-first personal operating system for life, projects, thoughts, execution, and insight.

It combines:
- a **command center** for what matters now
- a **second brain** for notes, ideas, and context
- a **project/life manager** for keeping domains separate
- an **insight layer** that gives useful stats without becoming a bureaucracy simulator

It is a **life OS first**.

---

See [`ROADMAP.md`](./ROADMAP.md) for the phased roadmap.
See [`AGENTS.md`](./AGENTS.md) for agent/contributor rules and project guardrails.

---

## Status

TajsOS is currently in extremely experimental state and might change heavily. 
- Currently supports **Android** and **Desktop** targets.

## Contributing

For contributors, here are useful Gradle commands to build and test:

- **Run full test suite**: `./gradlew test`
- **Run server tests**: `./gradlew :server:test`
- **Run shared logic tests (JVM target)**: `./gradlew :shared:cleanTest :shared:jvmTest`
- **Run UI component tests (Compose JVM)**: `./gradlew :composeApp:jvmTest`

To run the app locally:
- **Run Desktop JVM App**: `./gradlew :composeApp:run`
- **Run Ktor Server**: `./gradlew :server:run`
