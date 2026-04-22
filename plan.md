1.  **Address Review Feedback:**
    -   The reviewer correctly pointed out that adding `LaunchedEffect` here isn't just an optimization if it just makes it tick; however, it's replacing a direct call to `Clock.System.now()` inside a composable block. In Compose, calling a changing value like `Clock` directly without wrapping it in state means it either won't update, or if the parent forces recomposition, it evaluates on every frame. The memory instruction specifically says: "never pass constantly changing values (like Clock.System.now().toEpochMilliseconds()) as keys to remember blocks to avoid constant cache invalidation. Instead, wrap the time variable in a slower-updating state (e.g., a State updated every 60 seconds via a LaunchedEffect delay loop) or use derivedStateOf to ensure time-sensitive calculations stay fresh without triggering full recompositions on every frame."
    -   Since this is a duration timer that requires per-second updates to display "hh:mm:ss", the loop needs to be 1 second.
    -   I must also add KDocs to the code as requested by the user.

2.  **Implementation Strategy (KDoc & Refined optimization):**
    -   Add a KDoc to the `FocusCard` component explaining its purpose and the performance optimization of using a localized ticking state.
    -   Modify `composeApp/src/commonMain/kotlin/com/tajemniktv/tajsos/ui/components/cards/DashboardCards.kt` using a Python script via `run_in_bash_session`.

3.  **Refactoring `FocusCard`:**
    -   Add KDoc to `FocusCard`:
        ```kotlin
        /**
         * A dashboard card displaying the current focus session status.
         *
         * To prevent excessive recompositions from reading the system clock directly on every frame,
         * this component maintains a localized [currentMillis] state updated every second via a [LaunchedEffect].
         *
         * @param viewModel The main view model providing node data.
         * ... (other params)
         */
        @Composable
        fun FocusCard(...)
        ```

4.  **Verification:**
    -   Use `grep` and `cat` to ensure the file is modified correctly.
    -   Use `./gradlew :composeApp:compileKotlinJvm` to ensure compilation works.

5.  **Pre-commit Steps:**
    -   Complete pre-commit steps to ensure proper testing, verification, review, and reflection are done.

6.  **Submit PR:**
    -   Create a branch and submit the PR with title "perf: Use LaunchedEffect for FocusCard duration to optimize recomposition".
