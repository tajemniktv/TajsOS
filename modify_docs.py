import re

file_path = "shared/src/commonMain/kotlin/com/tajemniktv/tajsos/data/ModeQueryProfile.kt"
with open(file_path, "r") as f:
    content = f.read()

# I see ModeFilterProfile is already documented with @property instead of @param since it's a data class, wait, data classes use @property.
# Wait, @property is for properties, @param is also valid in Kotlin for constructor arguments.
