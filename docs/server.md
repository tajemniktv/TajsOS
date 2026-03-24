# TajsOS Server (Backend)

The `:server` module contains the Ktor-based backend application for TajsOS. Because TajsOS follows a "local-first" approach, the server's primary role is to facilitate synchronization and potential remote access, rather than being the absolute source of truth.

## Current Role & Limitations

*   **Foundation for Sync:** The server is currently scaffolding and foundation for a robust, conflict-resolving sync engine.
*   **Security by Default (Binding):** Out of the box, the Ktor server is configured to bind strictly to `127.0.0.1` (localhost). This is a security feature to ensure that a locally running backend is not accidentally exposed to external networks.
*   **Module Dependencies:** The `:server` module correctly depends on the `:shared` module, ensuring that the same Data Transfer Objects (DTOs) and Models can be used seamlessly across the backend and the clients.

## How to Run the Server

You can run the server using the standard Gradle application plugin tasks:

```bash
./gradlew :server:run
```

### Exposing the Server (Development/Testing)

If you need to test synchronization from a physical device (like an Android phone on the same Wi-Fi network), binding to `127.0.0.1` will fail because the device cannot reach the host machine.

You can override the binding host using the `SERVER_HOST` environment variable:

```bash
SERVER_HOST=0.0.0.0 ./gradlew :server:run
```

This tells Ktor to bind to all available network interfaces on the host machine.

## Future Plans

The eventual goal for the server is to support:

1.  **Multi-device Sync:** Receiving deltas (`NodeSnapshotEntity` or `EventLogEntity` records) from clients and merging them.
2.  **Public API:** Providing an authenticated API so external tools (like a browser extension or a webhook from Zapier) can inject nodes into the user's TajsOS instance.
