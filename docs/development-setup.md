# Development Setup

This guide will help you set up your environment to build and run TajsOS across its supported platforms.

## 1. Prerequisites

Before cloning the repository, ensure you have the following installed:

*   **Java Development Kit (JDK):** Version 17 or higher.
*   **Android Studio / IntelliJ IDEA:** Use the latest stable version of Android Studio (recommended for Android/Compose Multiplatform development) or IntelliJ IDEA Ultimate/Community with the Kotlin Multiplatform plugin installed.
*   **Node.js & npm:** (Optional) Required only if you intend to work on the Astro `website/` project.

## 2. Setting Up the Project

1.  **Clone the Repository:**
    ```bash
    git clone <repository-url>
    cd tajsos
    ```

2.  **Make the Gradle Wrapper Executable:**
    On Linux/macOS, ensure the Gradle wrapper has execution permissions:
    ```bash
    chmod +x gradlew
    ```

3.  **Open the Project:**
    Open the root `tajsos` folder in Android Studio or IntelliJ IDEA. Let the IDE sync the Gradle project.

## 3. Running the Application

TajsOS is a Kotlin Multiplatform project. You can run specific targets using Gradle commands or your IDE's run configurations.

### Desktop (JVM) - Primary Target

This is the fastest way to run and test the Compose UI.

*   **Via CLI:**
    ```bash
    ./gradlew :composeApp:run
    ```
*   **Via IDE:** Look for a run configuration named something like `composeApp [run]` or create a Gradle run configuration executing the `:composeApp:run` task.

### Android - Primary Target

*   **Via IDE (Recommended):** Select the `androidApp` run configuration and click "Run" (targeting an emulator or physical device).
*   **Via CLI:**
    ```bash
    ./gradlew :androidApp:installDebug
    ```

### Server (Ktor Backend)

The server handles local sync and remote functionality.

*   **Via CLI:**
    ```bash
    ./gradlew :server:run
    ```
*   *Note: By default, the Ktor server binds to `127.0.0.1` for security. Set the `SERVER_HOST` environment variable if you need it accessible on your local network (e.g., `SERVER_HOST=0.0.0.0 ./gradlew :server:run`).*

### Website (Astro)

The documentation and marketing site is located in the `website/` folder.

*   **Via CLI:**
    ```bash
    cd website
    npm install
    npm run dev &
    ```

## 4. Troubleshooting Common Issues

### Gradle Daemon Hangs or Build Freezes

If the build environment hangs (especially during the "calculating task graph" phase):

1.  **Kill stuck Java processes:** Use your system's task manager or `pkill -f java` / `killall java` to terminate stuck Gradle daemon processes.
2.  **Run without the daemon:** Add `--no-daemon` to your command:
    ```bash
    ./gradlew :composeApp:run --no-daemon
    ```
3.  **Disable configuration cache:** Add `--no-configuration-cache`:
    ```bash
    ./gradlew :composeApp:run --no-configuration-cache
    ```

### Wrapper Download Timeout

If `./gradlew` fails due to network timeouts while downloading the Gradle distribution, you can try using a locally installed `gradle` binary (if you have one installed via Homebrew, apt, etc.):
```bash
gradle :composeApp:run
```

### Ambiguous `compileKotlin` Task

When trying to compile just the Compose App for the JVM target, the generic `compileKotlin` task might fail or be ambiguous across KMP targets. Use the specific task instead:

```bash
./gradlew :composeApp:compileKotlinJvm
```
