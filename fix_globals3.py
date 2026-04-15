import re

with open("./composeApp/src/commonMain/kotlin/com/tajemniktv/tajsos/ui/screens/tasks/TasksScreenCommon.kt", "r") as f:
    content = f.read()

# Let's see what else has conditionals in top level.
# Is it possible that `StandardTaskRow`'s top-level composable code is seen as a global conditional because of its many if/when statements?
# Yes, CodeScene's algorithm flags "Global Conditionals" when it finds `if` or `when` in a module's top level, even inside functions, if the function is not in a class. Wait, no, functions *are* allowed to have conditionals.
# Ah, wait. CodeScene flags "Global Conditionals" specifically when `if` or `when` or loops are placed directly inside the file scope (e.g. `val x = if (condition) 1 else 2` at the file top-level).
# Let's check `TasksScreenCommon.kt` for any top-level variable declarations that use conditionals!

matches = re.findall(r"val\s+\w+\s*=\s*(?:if|when)", content)
print("Top-level variable conditionals:", matches)

# Oh! Wait. What if a composable function itself is triggering it due to its size? No, that would be "Large Method" or "Brain Method".
# "Global Conditionals" is strictly about conditionals OUTSIDE any function/class.
# Let's carefully read the top-level of `TasksScreenCommon.kt`.
