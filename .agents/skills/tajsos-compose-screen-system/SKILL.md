---
name: tajsos-compose-screen-system
description: TajsOS Compose screen-system overlay. Use for shell/screen/content ownership boundaries and consistent screen structure decisions.
---

# TajsOS Compose Screen System Overlay

## Ownership boundaries

- `AppShell` owns persistent chrome, route host placement, shell insets, responsive sidebar/header
  behavior, and shell-level overlays.
- `ScreenScaffold` owns screen-level structure, page header/title/actions, standard content insets,
  scroll behavior, width handling, and optional multi-pane layouts.
- Route/content composables own state gathering and block rendering.

## Consistency rules

- Avoid ad hoc screen-level `BoxWithConstraints` unless justified by a concrete layout need.
- Avoid one-off padding and random spacing unless justified by screen-specific constraints.
- Avoid custom scroll containers when existing scaffold patterns already solve the behavior.
- Avoid custom max-width logic unless existing screen scaffold width policy is insufficient.
- Do not introduce new shell/scaffold abstractions unless existing repo primitives cannot reasonably
  support the use case.

## Implementation reference usage

- Use `compose-skill` for Compose API and implementation details.
- Keep TajsOS screen architecture as the governing structure.

## Verified primitives

- Current repo primitives include `AppShell`, `ScreenScaffold`, and `SplitScreenScaffold`.
