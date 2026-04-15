import re

# Read the file again and make sure `scoreTask` is no longer a top-level function.
with open("./composeApp/src/commonMain/kotlin/com/tajemniktv/tajsos/ui/screens/tasks/TasksScreenCommon.kt", "r") as f:
    content = f.read()

# I also need to check `shortDate`. `shortDate` has no conditionals, so it shouldn't trigger "Global Conditionals".
# But `StandardTaskRow` is a Composable with many conditionals. Wait... composables ARE top level functions!
# CodeScene flags "Global Conditionals" for any file containing conditionals at the top-level (module scope) outside of a class/object.
# Kotlin top-level functions are very common. But if CodeScene is complaining, it's either `StandardTaskRow` or `scoreTask`.
# Let's wrap both `StandardTaskRow` and `TaskTabChip`, `StatusPill`, `DetailRow`, `ContextRow`, `shortDate`, etc., into an object?
# NO! Composables cannot easily be put into an object unless they are explicitly referenced. But top-level composables are standard in Jetpack Compose.
# Wait, in the provided memory:
# "The CI pipeline uses CodeScene which enforces 'Large Method' advisory rules. When writing tests or Jetpack Compose UI functions (e.g., creating complex lists/blocks), extract cohesive logical blocks into private helper methods or `@Composable` functions to prevent failing the Code Health Quality Gate."
# Wait, the failure is exactly: "1 file with Global Conditionals" in `TasksScreenCommon.kt`.
# A "Global Conditional" in CodeScene usually means `if` statements or `when` blocks in top-level functions.
# CodeScene sometimes gets confused by Compose's `@Composable` top-level functions because they're top-level functions with branching.
# However, the previous state of the file had NO violations. I added `StandardTaskRow` to the file.
# `StandardTaskRow` has several `if` statements.
# Let's extract the `ContextRow` and similar smaller bits of `StandardTaskRow` into smaller Composables, OR wrap the non-composable logic.

content = content.replace("internal object TaskScoring", "internal object TaskScoring")

with open("./composeApp/src/commonMain/kotlin/com/tajemniktv/tajsos/ui/screens/tasks/TasksScreenCommon.kt", "w") as f:
    f.write(content)
