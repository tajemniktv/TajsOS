import re

with open("./composeApp/src/commonMain/kotlin/com/tajemniktv/tajsos/ui/screens/tasks/TasksScreenCommon.kt", "r") as f:
    content = f.read()

# Fix the compile error (TasksAllView uses scoreTask and can't find it). Oh wait!
# e: file:///app/composeApp/src/commonMain/kotlin/com/tajemniktv/tajsos/ui/screens/tasks/TasksAllView.kt:124:30 Cannot infer type for type parameter 'R'. Specify it explicitly.
# e: file:///app/composeApp/src/commonMain/kotlin/com/tajemniktv/tajsos/ui/screens/tasks/TasksAllView.kt:125:25 Unresolved reference 'scoreTask'.
