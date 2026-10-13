---
name: tajsos-kmp-architecture
description: TajsOS KMP architecture overlay. Use for commonMain vs platform boundaries, shared state placement, and platform abstraction decisions.
---

# TajsOS KMP Architecture Overlay

## Shared-first boundaries

- Keep domain models, UI models, repositories/interfaces, and shared state logic in `commonMain`
  when practical.
- Keep Android-only lifecycle, permissions, notifications, platform storage, and system UI concerns
  out of `commonMain`.

## Android-skill usage constraints

- Do not blindly apply Android-only skills to shared KMP code.
- Treat Android-focused skills as platform references, not default architecture for `commonMain` or
  desktop code.

## Platform abstraction rules

- Use `expect/actual` only when the platform boundary is real, stable, and currently needed.
- Avoid speculative platform abstractions before the codebase needs them.

## Priority reminder

When guidance conflicts, repo reality and canonical docs still outrank this overlay.
