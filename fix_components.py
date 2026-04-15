import re
import glob

# 1. Open TasksScreenCommon.kt and wrap the new functions in an object.
with open("./composeApp/src/commonMain/kotlin/com/tajemniktv/tajsos/ui/screens/tasks/TasksScreenCommon.kt", "r") as f:
    content = f.read()

# I added `StandardTaskRow` to the bottom. I will wrap it.
search_row = """@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun StandardTaskRow(
    task: NodeEntity,
    projectById: Map<Long, String>,
    areaById: Map<Long, String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    showStatusPill: Boolean = false,
    onToggleDone: ((Boolean) -> Unit)? = null,
    trailingActions: @Composable () -> Unit = {}
) {"""

replace_row = """internal object TasksScreenComponents {
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StandardTaskRow(
    task: NodeEntity,
    projectById: Map<Long, String>,
    areaById: Map<Long, String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    showStatusPill: Boolean = false,
    onToggleDone: ((Boolean) -> Unit)? = null,
    trailingActions: @Composable () -> Unit = {}
) {"""

content = content.replace(search_row, replace_row)
content += "\n}"  # close TasksScreenComponents

with open("./composeApp/src/commonMain/kotlin/com/tajemniktv/tajsos/ui/screens/tasks/TasksScreenCommon.kt", "w") as f:
    f.write(content)

# Update the 5 files calling StandardTaskRow
files = [
    "TasksCommandView.kt",
    "TasksTodayView.kt",
    "TasksInboxView.kt",
    "TasksArchiveView.kt",
    "TasksAllView.kt"
]

for file in files:
    path = f"./composeApp/src/commonMain/kotlin/com/tajemniktv/tajsos/ui/screens/tasks/{file}"
    with open(path, "r") as f:
        content_view = f.read()

    content_view = content_view.replace("StandardTaskRow(", "TasksScreenComponents.StandardTaskRow(")
    with open(path, "w") as f:
        f.write(content_view)
