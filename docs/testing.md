# Testing TajsOS

TajsOS aims for high reliability through automated testing. Since the core logic is isolated in the `:shared` module and the UI is separated via ViewModels, the majority of testing is done without needing an Android emulator or UI automation.

## 1. Running Tests

You can run tests for the entire project or target specific modules using the Gradle wrapper.

**Run All Tests (Repo-wide):**
```bash
./gradlew test
```

**Run `:shared` Module Tests (JVM Target):**
This is where the bulk of the business logic, repository, and data model tests live.
```bash
./gradlew :shared:cleanTest :shared:jvmTest
```

**Run a Specific Test Class in `:shared`:**
```bash
./gradlew :shared:testJvmTest --tests "com.tajemniktv.tajsos.ClassName"
```

**Run `:composeApp` Module Tests (JVM Target):**
This tests the ViewModels and UI logic.
```bash
./gradlew :composeApp:jvmTest
```

**Run `:server` Module Tests:**
Tests the Ktor backend endpoints and logic.
```bash
./gradlew :server:test
```

## 2. Test Stack & Conventions

The testing environment utilizes several key libraries, primarily in the `commonTest` and `jvmTest` source sets.

*   **Kotlin Test:** Standard assertions (`assertEquals`, `assertTrue`, etc.).
*   **Coroutines Test (`kotlinx-coroutines-test`):** Used for controlling virtual time in suspending functions.
    *   *Note:* There is a known name clash in `gradle/libs.versions.toml` regarding `kotlinx.coroutines.test` and `kotlinx.coroutinesTest`. Modify versions with caution.
*   **Turbine (`app.cash.turbine:turbine`):** The primary tool for testing Kotlin `Flow` and `StateFlow` emissions. It provides a clean, sequential API (e.g., `flow.test { awaitItem() }`).
*   **MockK (`io.mockk:mockk`):** Used in JVM tests for mocking dependencies (like DAOs when testing the Repository, or the Repository when testing ViewModels).

## 3. General Testing Philosophy

*   **Proactive Testing:** Practice Test-Driven Development (TDD) when practical. Write a failing test for a bug before fixing it, or write tests for a new feature before implementing the logic.
*   **Focus on the Core:** The highest priority for testing is the `:shared` module (Repositories, data transformations, domain logic) and the ViewModels in `:composeApp` (state management and intent handling).
*   **Avoid UI Tests Initially:** Focus on testing the ViewModels' `StateFlow` outputs rather than instrumented Compose UI tests, as UI logic is mostly declarative and mapping state to screen.

## 4. Temporary Kotlin Snippets

Because the standalone `kotlinc` compiler is not available in the default environment, if you need to quickly test a Kotlin snippet, the recommended approach is:

1.  Create a temporary test file in `shared/src/commonTest/kotlin/`.
2.  Write your snippet inside a standard `@Test` function.
3.  Execute it via Gradle: `./gradlew :shared:jvmTest --tests "YourTempTestClass"`.
