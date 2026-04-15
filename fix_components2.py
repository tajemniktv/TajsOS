import re

with open("./composeApp/src/commonMain/kotlin/com/tajemniktv/tajsos/ui/screens/tasks/TasksAllView.kt", "r") as f:
    content = f.read()

content = content.replace("scoreTask(", "TaskScoring.scoreTask(")

with open("./composeApp/src/commonMain/kotlin/com/tajemniktv/tajsos/ui/screens/tasks/TasksAllView.kt", "w") as f:
    f.write(content)
