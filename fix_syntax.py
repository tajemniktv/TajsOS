import re

with open("./composeApp/src/commonMain/kotlin/com/tajemniktv/tajsos/ui/screens/tasks/TasksScreenCommon.kt", "r") as f:
    content = f.read()

# Let's see lines 210-214 of TasksScreenCommon.kt
# They are:
# 209:                if (contextStr.isNotBlank()) {
# 210:                    Text(contextStr, style = MaterialTheme.typography.bodySmall, color = TajsOSTheme.Muted)
# 211:                }

print("Looking at lines 205-215")
lines = content.split('\n')
for i in range(205, 215):
    if i < len(lines):
        print(f"{i+1}: {lines[i]}")

# Wait, `TasksScreenComponents` object has syntax errors:
# "TasksScreenCommon.kt:29:28 Conflicting import: imported name 'Modifier' is ambiguous"
# Oh! I imported Modifier twice in my previous script (`import androidx.compose.ui.Modifier\n` at line 29 AND 31 or something).
# Let's fix that.

import_search = "import androidx.compose.ui.Modifier\nimport androidx.compose.ui.graphics.Color\nimport androidx.compose.ui.text.font.FontWeight\nimport androidx.compose.ui.Modifier\n"
import_replace = "import androidx.compose.ui.graphics.Color\nimport androidx.compose.ui.text.font.FontWeight\nimport androidx.compose.ui.Modifier\n"
content = content.replace(import_search, import_replace)

with open("./composeApp/src/commonMain/kotlin/com/tajemniktv/tajsos/ui/screens/tasks/TasksScreenCommon.kt", "w") as f:
    f.write(content)
