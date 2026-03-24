# TajsOS

**TajsOS** is a local-first personal operating system for life, projects, thoughts, execution, and insight.

It combines:
- a **command center** for what matters now
- a **second brain** for notes, ideas, and context
- a **project/life manager** for keeping domains separate
- an **insight layer** that gives useful stats without becoming a bureaucracy simulator

It is a **life OS first**.

---

## Documentation

For more detailed information, please explore the `docs/` directory and related files:

- [AGENTS.md](./AGENTS.md) - Agent/contributor rules and project guardrails.
- [ROADMAP.md](./ROADMAP.md) - Phased roadmap and current progress.
- [DESIGN.md](./DESIGN.md) - Design system and visual language rules.
- [docs/architecture.md](./docs/architecture.md) - System architecture and design choices.
- [docs/modules.md](./docs/modules.md) - Detailed module breakdown.
- [docs/domain-model.md](./docs/domain-model.md) - Core data entities and relationships.
- [docs/development-setup.md](./docs/development-setup.md) - Environment setup and run instructions.
- [docs/testing.md](./docs/testing.md) - Testing conventions and commands.
- [docs/server.md](./docs/server.md) - Ktor backend documentation.
- [docs/website.md](./docs/website.md) - Astro website documentation.

---

## Status and Targets

TajsOS is currently in an **extremely experimental state** and might change heavily.

**Supported Targets:**
- **Android**: Primary mobile target.
- **Desktop (JVM)**: Primary desktop target.
- **Server**: Local-first sync/remote backend (Ktor).
- **iOS & Web**: Scaffolded, but not currently the primary focus.

---

## Quick Start & How to Run

### Prerequisites
- JDK 17+
- Android Studio (for Android/Compose) or IntelliJ IDEA.
- Gradle (use the wrapper provided `./gradlew` or `gradlew.bat`). Note: Make sure the wrapper is executable (`chmod +x gradlew`).

### Running the App

- **Desktop (JVM)**:
  ```bash
  ./gradlew :composeApp:run
  ```
- **Android**: Run the `:androidApp` target from Android Studio, or use:
  ```bash
  ./gradlew :androidApp:installDebug
  ```
- **Server**:
  ```bash
  ./gradlew :server:run
  ```
- **Website**:
  ```bash
  cd website
  npm install
  npm run dev &
  ```

For more detailed setup instructions, see [Development Setup](./docs/development-setup.md).

---

## High-Level Module Map

- `:shared` - Core business logic, Room entities, repositories, and data models.
- `:composeApp` - Shared Compose UI, navigation, and screens for all platforms.
- `:androidApp` - Android-specific entry point and manifest.
- `:server` - Ktor-based backend for sync and remote features.
- `:iosApp` - iOS native entry point (scaffolded).
- `website/` - Astro-based documentation/marketing site.

See [Modules](./docs/modules.md) for more details.

---

## Development Workflow

1. **Pick an issue or feature**: Review `ROADMAP.md` and `AGENTS.md` before starting.
2. **Make changes**:
   - Core data and logic go in `:shared`.
   - UI components go in `:composeApp`.
   - Prefer small, local polish over invention. Do not introduce new dependencies or alter core architecture unless necessary and justified.
3. **Verify locally**: Run tests and build the affected targets.
4. **Submit**: Use short, descriptive commit messages and focused PRs.

---

## Testing & Build Basics

Run the full test suite:
```bash
./gradlew test
```

Test specific modules:
- Shared module (JVM tests): `./gradlew :shared:cleanTest :shared:jvmTest`
- Compose UI module (JVM target): `./gradlew :composeApp:jvmTest`
- Server module: `./gradlew :server:test`

If you encounter build hangs during the task graph calculation phase, try appending `--no-daemon` or `--no-configuration-cache` (and ensure stalled java processes are killed).

See [Testing](./docs/testing.md) for more details.

---

## Where to Look First

- **Entry Points**:
  - UI: `composeApp/src/commonMain/kotlin/com/tajemniktv/tajsos/App.kt`
  - Data: `shared/src/commonMain/kotlin/com/tajemniktv/tajsos/data/Repository.kt`
  - Android: `androidApp/src/main/java/com/tajemniktv/tajsos/MainActivity.kt`
- **Database**: `shared/src/commonMain/kotlin/com/tajemniktv/tajsos/data/AppDatabase.kt`
- **UI Screens**: `composeApp/src/commonMain/kotlin/com/tajemniktv/tajsos/ui/screens/`

