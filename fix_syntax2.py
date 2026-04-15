import re

with open("./composeApp/src/commonMain/kotlin/com/tajemniktv/tajsos/ui/screens/tasks/TasksScreenCommon.kt", "r") as f:
    content = f.read()

# Let's see what is after line 215. The error said:
# "TasksScreenCommon.kt:210:17 Syntax error: Expecting ')'."
# Wait. `FlowRow(`
# The parameters `horizontalArrangement` and `verticalArrangement` for `FlowRow` are inside `androidx.compose.foundation.layout.FlowRow`.
# Did my previous script mess up the brackets? Let's check the end of the file.

print("Looking at end of file")
lines = content.split('\n')
for i in range(max(0, len(lines)-30), len(lines)):
    print(f"{i+1}: {lines[i]}")
