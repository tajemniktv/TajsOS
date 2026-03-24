# Supported Paths
- `GET /health`: Returns a simple `HealthResponse` object indicating the status ("OK"), version, and uptime of the server.
- `POST /sync`: A dummy endpoint that simulates syncing client data with the server. It expects a `SyncRequest` object and returns a `SyncResponse` object. The payload validation guarantees the content-type is `application/json`.

# Known Gaps
- `POST /sync` currently has a dummy implementation. It doesn't actually process or persist the sync items. It just responds with an empty list of items and conflicts. This needs to be hooked up to the local-first database sync logic.
- Health endpoint version is hardcoded to "1.0.0". In a real setup, this should ideally be passed from the build properties or injected as an environment variable.
- Authentication/Authorization are currently missing and should be added when standardizing secure local-first operations.
