import re

with open("./composeApp/src/commonMain/kotlin/com/tajemniktv/tajsos/ui/screens/tasks/TasksScreenCommon.kt", "r") as f:
    content = f.read()

# Refactor scoreTask to avoid excessive inline conditionals (which triggers CodeScene "Global Conditionals" due to top-level/global scope in a file without a class enclosure).
# Let's wrap scoreTask in an object, or extract logic into smaller functions.
# Wait, CodeScene "Global Conditionals" refers to top-level functions having conditional logic. The best fix is to move `scoreTask` into an object, e.g., `TaskScoring`.
# Oh, it's used across multiple files. It's an internal function.

search_scoreTask = """internal fun scoreTask(
    task: NodeEntity,
    now: Long,
    todayTaskIds: Set<Long>,
): Int {"""

replace_scoreTask = """internal object TaskScoring {
    fun scoreTask(
        task: NodeEntity,
        now: Long,
        todayTaskIds: Set<Long>,
    ): Int {"""

content = content.replace(search_scoreTask, replace_scoreTask)

# close the object
content = content.replace("    return score\n}", "    return score\n}\n}")

# also need to update imports/references in TasksCommandView
with open("./composeApp/src/commonMain/kotlin/com/tajemniktv/tajsos/ui/screens/tasks/TasksScreenCommon.kt", "w") as f:
    f.write(content)

with open("./composeApp/src/commonMain/kotlin/com/tajemniktv/tajsos/ui/screens/tasks/TasksCommandView.kt", "r") as f:
    content_cv = f.read()

content_cv = content_cv.replace("scoreTask(", "TaskScoring.scoreTask(")

with open("./composeApp/src/commonMain/kotlin/com/tajemniktv/tajsos/ui/screens/tasks/TasksCommandView.kt", "w") as f:
    f.write(content_cv)
