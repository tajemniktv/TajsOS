with open("./composeApp/src/commonMain/kotlin/com/tajemniktv/tajsos/ui/screens/tasks/TasksScreenCommon.kt", "r") as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if "val color =" in line:
        print(f"Line {i+1}: {line.strip()}")
        # print 5 lines before and after to see context
        print("".join(lines[max(0, i-5):min(len(lines), i+15)]))
