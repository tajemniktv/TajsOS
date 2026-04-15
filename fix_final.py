import re

with open("./composeApp/src/commonMain/kotlin/com/tajemniktv/tajsos/ui/screens/tasks/TasksScreenCommon.kt", "r") as f:
    content = f.read()

# According to CodeScene documentation, "Global Conditionals" applies to files where conditional logic (if/when) exists OUTSIDE of a class or object structure.
# Even inside functions! If a function is top-level (like `StatusPill`, `scoreTask`, `StandardTaskRow`), its internal if/when logic triggers this rule.
# CodeScene prefers functions with conditionals to be grouped inside an `object` or `class`.
# Since `@Composable` functions must often be top-level or inside an object but not a class, we could try moving `StandardTaskRow`, `StatusPill`, `DetailRow`, `ContextRow`, `TaskTabChip` etc. inside an object, say `TasksCommonComponents`, but unfortunately `@Composable` functions inside `object`s can sometimes have slight ergonomic issues with imports, though they are completely legal.
# Let's just wrap everything in the file into an object `TasksScreenCommon` or similar? No, the file is `TasksScreenCommon.kt`.
# Wait! Instead of wrapping EVERYTHING, let's wrap just `StandardTaskRow`? Wait, I added `StandardTaskRow`. Did the file fail BEFORE my PR? No, the violation was added by my PR. So `StandardTaskRow` is the culprit.
# `StandardTaskRow` has multiple `if` and `?.let` checks. Let's wrap `StandardTaskRow` inside a file-private or internal `object TasksScreenComponents`.
# And then replace its usage in `TasksScreenCommon.kt` and other files.
# WAIT, `@Composable` inside an `object` is fine!
# But actually, I don't want to rename the component and break other files again.

# Let's extract the internals of StandardTaskRow or just wrap the `if`s? No, CodeScene triggers on ANY conditional.
# "Global Conditionals: 1 file with Global Conditionals".
# What if we just suppress it? Oh! We have the exact URL to suppress it in the logs:
# `[Suppress](https://codescene.io/projects/78234/delta?repo-id=1505272&review-id=297&biomarker=Global+Conditionals&filename=composeApp%2Fsrc%2FcommonMain%2Fkotlin%2Fcom%2Ftajemniktv%2Ftajsos%2Fui%2Fscreens%2Ftasks%2FTasksScreenCommon.kt&method=&suppress=true)`
# Wait, we can't click that link.
# How do we fix it in the code? CodeScene ignores the violation if there's a specific comment, or maybe we just put the composable into an object?
# "CodeScene prefers to avoid top-level functions that contain logic. However, for Jetpack Compose, this is the standard pattern."
# If I wrap the `StandardTaskRow` in an object, I'd have to update usages in `TasksCommandView`, `TasksTodayView`, `TasksInboxView`, `TasksArchiveView`, `TasksAllView`.
# That's easy enough.
