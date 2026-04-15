import re

with open("./composeApp/src/commonMain/kotlin/com/tajemniktv/tajsos/ui/screens/tasks/TasksScreenCommon.kt", "r") as f:
    content = f.read()

# I see the Android tests failed due to processDebugGoogleServices missing a valid project_info. Wait, we mocked it before, but the runner on Github actions might be failing on processDebugGoogleServices or something related. Let's look at the exact error:
# "Failed to find package 'platforms;android-37'"
# Ah! The Github Actions runner is trying to download SDK tools and it fails to find `platforms;android-37`.
# Why? Because in `gradle/libs.versions.toml` the compileSdk is 37? No, wait.
# "COMPILE_SDK="$(grep -Eo 'android-compileSdk\s*=\s*"[0-9]+"' gradle/libs.versions.toml | sed -E 's/.*"([0-9]+)".*/\1/' || true)"
# "Failed to find package 'platforms;android-37'"
# It means Android API 37 doesn't exist yet! Why is it set to 37? Let's check `libs.versions.toml`.
