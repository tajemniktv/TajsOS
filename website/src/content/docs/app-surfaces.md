---
title: App Surfaces & UI Layering
description: How the UI is constructed.
---

The Compose app separates UI composition into explicit layers:

- **`AppShell`**: For persistent chrome such as the sidebar, top header, shell spacing, and route host.
- **`ScreenScaffold` / `SplitScreenScaffold`**: For reusable page-level structure, width policy, and scroll behavior.
- **Screen Route/Content Composables**: For state collection and screen-specific rendering only.
