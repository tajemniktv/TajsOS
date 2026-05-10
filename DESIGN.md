---
version: "alpha"
name: "TajsOS"
description: "A local-first calm life operating system for tasks, notes, protocols, domains, planning, knowledge, and review."

colors:
  background: "#101114"
  on-background: "#E6E0E9"

  surface: "#15171C"
  surface-dim: "#141218"
  surface-bright: "#3B383F"
  surface-container-lowest: "#0F0D13"
  surface-container-low: "#1D1B21"
  surface-container: "#211F25"
  surface-container-high: "#2B292F"
  surface-container-highest: "#36343A"
  surface-variant: "#36343A"

  on-surface: "#E6E0E9"
  on-surface-variant: "#CBC4D3"
  on-surface-muted: "#948E9C"

  inverse-surface: "#E6E0E9"
  inverse-on-surface: "#322F36"

  outline: "#948E9C"
  outline-variant: "#494551"

  primary: "#D3BFFF"
  on-primary: "#391B77"
  primary-container: "#BA9EFF"
  on-primary-container: "#4B2F89"
  primary-fixed: "#E9DDFF"
  primary-fixed-dim: "#D0BCFF"
  on-primary-fixed: "#23005C"
  on-primary-fixed-variant: "#50358F"
  inverse-primary: "#684EA8"
  surface-tint: "#D0BCFF"

  secondary: "#C4C6CE"
  on-secondary: "#2D3037"
  secondary-container: "#464950"
  on-secondary-container: "#B6B8C0"
  secondary-fixed: "#E1E2EA"
  secondary-fixed-dim: "#C4C6CE"
  on-secondary-fixed: "#191C22"
  on-secondary-fixed-variant: "#44474D"

  tertiary: "#D7CD4C"
  on-tertiary: "#353100"
  tertiary-container: "#BBB132"
  on-tertiary-container: "#484300"
  tertiary-fixed: "#F1E662"
  tertiary-fixed-dim: "#D4CA49"
  on-tertiary-fixed: "#1F1C00"
  on-tertiary-fixed-variant: "#4D4800"

  error: "#FFB4AB"
  on-error: "#690005"
  error-container: "#93000A"
  on-error-container: "#FFDAD6"

  success: "#29B487"
  danger: "#E05A68"
  warning: "#D9A441"
  info: "#5C8DFF"

typography:
  headline-display:
    fontFamily: "Space Grotesk"
    fontSize: 40px
    fontWeight: 700
    lineHeight: 1.12
    letterSpacing: -0.035em

  headline-xl:
    fontFamily: "Space Grotesk"
    fontSize: 32px
    fontWeight: 700
    lineHeight: 1.2
    letterSpacing: -0.025em

  headline-lg:
    fontFamily: "Space Grotesk"
    fontSize: 24px
    fontWeight: 700
    lineHeight: 1.2
    letterSpacing: -0.025em

  title-md:
    fontFamily: "Space Grotesk"
    fontSize: 18px
    fontWeight: 600
    lineHeight: 1.3
    letterSpacing: -0.015em

  body-lg:
    fontFamily: "Outfit"
    fontSize: 18px
    fontWeight: 400
    lineHeight: 1.6

  body-md:
    fontFamily: "Outfit"
    fontSize: 16px
    fontWeight: 400
    lineHeight: 1.65

  body-sm:
    fontFamily: "Outfit"
    fontSize: 14px
    fontWeight: 400
    lineHeight: 1.55

  label-lg:
    fontFamily: "Space Grotesk"
    fontSize: 16px
    fontWeight: 600
    lineHeight: 1.1

  label-md:
    fontFamily: "Space Grotesk"
    fontSize: 14px
    fontWeight: 600
    lineHeight: 1.2
    letterSpacing: 0.01em

  label-sm:
    fontFamily: "JetBrains Mono"
    fontSize: 12px
    fontWeight: 500
    lineHeight: 1.25
    letterSpacing: 0.04em

rounded:
  none: 0px
  xs: 4px
  sm: 8px
  md: 12px
  lg: 16px
  xl: 24px
  2xl: 32px
  full: 9999px

spacing:
  xs: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 32px
  2xl: 48px
  3xl: 64px
  4xl: 96px
  grid-unit: 8px
  margin-mobile: 16px
  margin-desktop: 32px
  gutter: 16px

components:
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    typography: "{typography.label-lg}"
    rounded: "{rounded.md}"
    height: 48px
    padding: 20px

  button-primary-pressed:
    backgroundColor: "{colors.primary-container}"
    textColor: "{colors.on-primary-container}"
    typography: "{typography.label-lg}"
    rounded: "{rounded.md}"
    height: 48px
    padding: 20px

  button-secondary:
    backgroundColor: "{colors.surface-container-high}"
    textColor: "{colors.on-surface}"
    typography: "{typography.label-lg}"
    rounded: "{rounded.md}"
    height: 48px
    padding: 20px

  button-tertiary:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.on-surface-variant}"
    typography: "{typography.label-md}"
    rounded: "{rounded.md}"
    height: 44px
    padding: 16px

  button-danger:
    backgroundColor: "{colors.error-container}"
    textColor: "{colors.on-error-container}"
    typography: "{typography.label-lg}"
    rounded: "{rounded.md}"
    height: 48px
    padding: 20px

  input-default:
    backgroundColor: "{colors.surface-container-lowest}"
    textColor: "{colors.on-surface}"
    typography: "{typography.body-md}"
    rounded: "{rounded.md}"
    height: 48px
    padding: 16px

  card-default:
    backgroundColor: "{colors.surface-container}"
    textColor: "{colors.on-surface}"
    typography: "{typography.body-md}"
    rounded: "{rounded.xl}"
    padding: 24px

  panel-elevated:
    backgroundColor: "{colors.surface-container-high}"
    textColor: "{colors.on-surface}"
    typography: "{typography.body-md}"
    rounded: "{rounded.2xl}"
    padding: 32px

  chip-default:
    backgroundColor: "{colors.surface-container-high}"
    textColor: "{colors.on-surface-variant}"
    typography: "{typography.label-sm}"
    rounded: "{rounded.full}"
    height: 32px
    padding: 12px

  chip-active:
    backgroundColor: "{colors.primary-fixed-dim}"
    textColor: "{colors.on-primary-fixed}"
    typography: "{typography.label-sm}"
    rounded: "{rounded.full}"
    height: 32px
    padding: 12px

  list-row:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.on-surface}"
    typography: "{typography.body-md}"
    rounded: "{rounded.lg}"
    height: 72px
    padding: 16px

  nav-item-active:
    backgroundColor: "{colors.primary-container}"
    textColor: "{colors.on-primary-container}"
    typography: "{typography.label-md}"
    rounded: "{rounded.full}"
    height: 44px
    padding: 16px

  command-palette:
    backgroundColor: "{colors.surface-container-highest}"
    textColor: "{colors.on-surface}"
    typography: "{typography.body-md}"
    rounded: "{rounded.2xl}"
    width: 720px
    padding: 24px

  capture-sheet:
    backgroundColor: "{colors.surface-container-highest}"
    textColor: "{colors.on-surface}"
    typography: "{typography.body-md}"
    rounded: "{rounded.2xl}"
    width: 640px
    padding: 24px

  status-success:
    backgroundColor: "{colors.success}"
    textColor: "{colors.background}"
    typography: "{typography.label-sm}"
    rounded: "{rounded.full}"
    height: 28px
    padding: 10px

  status-warning:
    backgroundColor: "{colors.warning}"
    textColor: "{colors.background}"
    typography: "{typography.label-sm}"
    rounded: "{rounded.full}"
    height: 28px
    padding: 10px

  status-danger:
    backgroundColor: "{colors.danger}"
    textColor: "{colors.background}"
    typography: "{typography.label-sm}"
    rounded: "{rounded.full}"
    height: 28px
    padding: 10px

  skeleton-block:
    backgroundColor: "{colors.surface-container-high}"
    rounded: "{rounded.lg}"
    height: 72px
    width: 320px
---

# Design System: TajsOS

## Overview

TajsOS is a local-first personal operating system for thoughts, tasks, projects, knowledge, routines, domains, protocols, and review. It should feel like a calm life instrument: precise, tactile, fast, emotionally safe, and useful under real daily pressure.

The product is designed with ADHD-aware clarity in mind without becoming a medicalized or childish interface. The app should help users move from mental noise into structured action through stable navigation, obvious next actions, calm surfaces, and reversible capture.

The creative north star is **Calm Life Instrument**. TajsOS should feel like a high-end planning desk, a focused cockpit, and a second-brain library merged into one coherent product. It may feel advanced, but it must not become sci-fi cosplay.

### Product lenses

Use these as stable top-level mental models:

- **Now:** immediate focus, current protocol, today's commitments, quick capture.
- **Plan:** calendar, schedule, time architecture, future commitments.
- **Operate:** tasks, projects, areas, execution queues, routines.
- **Knowledge:** notes, records, sources, archive, linked context.
- **Review:** reflection, weekly review, insights, history, trends.

### Core objects

Use these names consistently across UI, code, docs, prompts, and generated screens:

- **Inbox Entry:** raw captured item before classification.
- **Task:** actionable unit with state, priority, due context, and relations.
- **Note:** knowledge object, thought, draft, reference, or journal-like content.
- **Project:** outcome-oriented container with tasks, notes, and milestones.
- **Area:** ongoing responsibility without a fixed completion point.
- **Protocol:** reusable operating mode or routine that changes context.
- **Domain:** life category such as Health, Education, Finances, or Relationships.
- **Record:** structured historical entry such as mood, expense, medication, session, habit, or log.
- **Relation:** typed link between objects.

Domains are contextual lenses over the same life system, not folders. A task can belong to a project, appear in Now, relate to Health, and be affected by a protocol.

### Design dials

- Product screens: Creativity 6, Density 6, Variance 6, Motion Intent 5.
- Marketing and website screens: Creativity 8, Density 4, Variance 8, Motion Intent 6.
- Product screens prioritize clarity and repeatable workflows. Marketing pages may be more editorial, but must still use the same tokens.

## Colors

The default identity is dark-first Material-adjacent tonal layering with a single muted iris accent. Light mode should feel intentionally designed, not like a mechanical inversion.

### Primary palette

- **Background** (`#101114`): main dark app canvas.
- **Surface** (`#15171C`): default panels, shell areas, and stable app surfaces.
- **Surface Container Lowest** (`#0F0D13`): recessed fields, editor wells, inactive zones.
- **Surface Container Low** (`#1D1B21`): sidebar zones, section surfaces, secondary panels.
- **Surface Container** (`#211F25`): standard cards, sheets, toolbar containers.
- **Surface Container High** (`#2B292F`): raised components, selected states, active cards.
- **Surface Container Highest** (`#36343A`): popovers, menus, command palette, modal sheets.
- **On Surface** (`#E6E0E9`): primary text on dark surfaces.
- **On Surface Variant** (`#CBC4D3`): body text, descriptions, metadata.
- **Outline Variant** (`#494551`): ghost boundaries and accessible separators.

### Accent palette

- **Primary / Taj Iris** (`#D3BFFF`): active route, primary CTA, focus ring, selected protocol.
- **Primary Container** (`#BA9EFF`): pressed states and active containers.
- **On Primary** (`#391B77`): text on primary.
- **Inverse Primary** (`#684EA8`): light-mode inverse accent use.
- **Secondary** (`#C4C6CE`): neutral supporting controls and secondary actions.
- **Tertiary** (`#D7CD4C`): rare warm highlight, review signal, or reflective annotation.

Only one accent should dominate a screen. Purple is allowed because it belongs to the TajsOS identity, but it must be muted, tonal, and functional. Do not turn it into neon glow sludge.

### Semantic palette

- **Success** (`#29B487`): completed, healthy, confirmed.
- **Warning** (`#D9A441`): attention required, not danger.
- **Danger** (`#E05A68`): destructive actions, missed commitments, errors.
- **Info** (`#5C8DFF`): neutral informational system state.
- **Error** (`#FFB4AB`): validation errors and recoverable failures.

### Color usage rules

- Use tonal layering before borders or shadows.
- Use `primary` only for the most important interactive emphasis in a region.
- Status colors appear only when they encode meaning.
- Do not use pure black surfaces.
- Do not use rainbow dashboards.
- Do not use neon outer glows.
- Do not use large gradient text.
- Borders are allowed only as quiet ghost boundaries, not as page-wide section dividers.
- Primary CTAs may use a very subtle tonal shift, but text contrast must remain excellent.

## Typography

Typography should feel technical, warm, and composed. TajsOS is a productivity instrument, not a startup template generator having an identity crisis.

### Font roles

- **Display and headings:** Space Grotesk, 700. Used for route titles, major empty states, hero copy, and section titles.
- **Body:** Outfit, 400. Used for descriptions, forms, lists, notes, and general UI copy.
- **Labels:** Space Grotesk, 600. Used for buttons, tabs, navigation, settings labels.
- **Metadata:** JetBrains Mono, 500. Used for timestamps, IDs, protocol states, keyboard hints, and compact technical data.

If Space Grotesk or Outfit is unavailable in a native target, use the closest bundled geometric sans. Do not use Inter as the default identity.

### Scale

- **headline-display:** 40px, Space Grotesk 700, line-height 1.12, letter-spacing -0.035em.
- **headline-xl:** 32px, Space Grotesk 700, line-height 1.2, letter-spacing -0.025em.
- **headline-lg:** 24px, Space Grotesk 700, line-height 1.2, letter-spacing -0.025em.
- **title-md:** 18px, Space Grotesk 600, line-height 1.3, letter-spacing -0.015em.
- **body-lg:** 18px, Outfit 400, line-height 1.6.
- **body-md:** 16px, Outfit 400, line-height 1.65.
- **body-sm:** 14px, Outfit 400, line-height 1.55.
- **label-lg:** 16px, Space Grotesk 600, line-height 1.1.
- **label-md:** 14px, Space Grotesk 600, line-height 1.2.
- **label-sm:** 12px, JetBrains Mono 500, line-height 1.25, letter-spacing 0.04em.

### Typography rules

- Body copy should stay under 65 characters per line.
- Screen headings should be left-aligned by default.
- Hero headings must not wrap into five-line slabs. Use wider containers and smaller type before allowing ugly line breaks.
- Labels may use uppercase only for very short technical states.
- Metadata should be muted and monospaced, not visually louder than the task title.
- No generic serif fonts in app screens.
- No excessive gradient text on large headers.

## Layout

Layout is grid-first, shell-first, and workflow-first. Product screens should be stable enough for daily use while still feeling premium and intentional.

### Spacing

Use the spacing tokens exactly:

- **xs:** 4px for hair gaps and dense icon alignment.
- **sm:** 8px for small control gaps.
- **md:** 16px for default component spacing.
- **lg:** 24px for card padding and section gaps.
- **xl:** 32px for large group spacing.
- **2xl:** 48px for major screen spacing.
- **3xl:** 64px for hero and empty-state spacing.
- **4xl:** 96px for marketing section spacing only.

Compose Multiplatform may map these pixel values to equivalent dp tokens to preserve rhythm across Android and desktop.

### App shell

The shell is persistent. Routes change inside it.

- **Collapsed sidebar:** 72px.
- **Default sidebar:** 280px.
- **Expanded sidebar:** 320px maximum.
- **Top header:** 72px minimum.
- **Detail rail:** 360px to 420px when present.
- **Content max width:** 1440px for dense product screens.
- **Desktop content padding:** 24px minimum, 32px preferred.
- **Mobile margin:** 16px.
- **Desktop margin:** 32px.
- **Grid unit:** 8px.

### Shell zones

1. **Sidebar:** route groups, lens navigation, New Entry, account area.
2. **Header:** active route, current protocol, search or command affordance, notifications.
3. **Content Stage:** route content, responsive grid, primary workflow.
4. **Context Rail:** relations, metadata, history, attachments, inspector controls.
5. **Overlay Layer:** command palette, capture sheet, modals.

### Product layout patterns

Use these patterns before inventing new ones:

- **Asymmetric 8/4 split:** primary workflow plus context summary.
- **Master-detail:** list or grid on the left, detail or inspector on the right.
- **Rail plus stage:** content with persistent metadata/actions rail.
- **Bento dashboard:** 5 to 7 modules with intentional spans and no empty grid holes.
- **Dense list with preview:** task or note list with selected preview side panel.

### Screen recipes

#### Now Dashboard

Purpose: answer "what matters now?" within five seconds.

- Header with greeting, date, current protocol, and quick capture.
- Left column: active focus, next task, protocol actions.
- Right column: timeline, reminders, recent inbox entries.
- Bottom area: intelligence feed and resurfaced notes.
- One dominant CTA maximum, usually New Entry or Start Focus.

#### Greeting Screen

Purpose: a calm briefing before entering the system.

- Large left-aligned greeting.
- Offset context block with date, protocol, and next commitment.
- One primary action: Open Now.
- No centered motivational quote wallpaper energy.

#### Tasks

Purpose: execute and organize action.

- Views: Inbox, Today, Upcoming, Projects, Archive.
- Rows show status, title, due context, project/domain relation, and compact actions.
- Detail rail shows metadata, relations, notes, and history.
- Task rows are not mini cards unless selection elevation is needed.

#### Notes and Rich Editor

Purpose: create, connect, and retrieve knowledge.

- List or graph rail.
- Editor stage as a calm writing surface.
- Inspector for relations, backlinks, attachments, metadata, history.
- Clear View and Edit modes.
- Toolbar should be contextual and collapsible.

#### Protocol Library

Purpose: choose and maintain operating modes.

- Protocol cards show intent, triggers, active domains, and next action.
- Active protocol gets tonal treatment.
- Detail view shows steps, automations, schedule, and review history.
- Protocols are functional routines, not decorative mood presets.

#### Domains

Purpose: show life areas as contextual dashboards.

- Domain overview grid: Health, Education, Finances, Relationships, Work, Home, Creative.
- Each domain page contains records, tasks, notes, trends, and protocols linked to that domain.
- Domain cards show one primary signal, one risk, and one next action.

#### Settings

Purpose: configuration without losing the user in a filing cabinet.

- Left settings navigation.
- Main setting groups with descriptions and controls.
- Dangerous actions isolated at the bottom.
- Theme and accent settings preview the result before applying.

### Responsive rules

- Under 768px, every multi-column layout collapses to one column.
- Detail rails become bottom sheets or separate detail routes on mobile.
- Sidebar becomes a navigation rail or bottom navigation depending on platform convention.
- Touch targets must be at least 48px.
- No horizontal scroll in app screens.
- Use `min-height: 100dvh` on web, never `height: 100vh`.
- Do not shrink body text below readable size to force a layout to fit.

## Elevation & Depth

Depth is achieved mainly through tonal layering. Shadows exist, but they are quiet and functional.

### Tonal elevation

- **Level 0:** `background`; app canvas and full-screen base.
- **Level 1:** `surface-container-low`; sidebar zones and section surfaces.
- **Level 2:** `surface-container`; standard cards and panels.
- **Level 3:** `surface-container-high`; active panels and detail sheets.
- **Level 4:** `surface-container-highest`; popovers, command palette, capture sheet, modal shells.

### Shadow guidance

- Dark mode shadows should be broad and diffused, never sharp.
- Light mode shadows should be softer than web SaaS defaults.
- Accent shadow is allowed only for focus or active system state and must stay subtle.
- Use ghost outlines when accessibility requires a boundary.
- Never use neon button glows or pulsing outline effects.

### Layering rules

- Inner modules should sit on a higher surface tier than their parent.
- Avoid card nesting deeper than two visible levels.
- The editor surface should feel open and calm, not trapped inside a tiny card.
- Overlays should use scrim context plus Level 4 surfaces.

## Shapes

TajsOS shape language is soft-industrial: rounded enough to feel tactile, structured enough to stay serious.

### Radius scale

- **none:** 0px. Use rarely, mainly for charts or strict data visuals.
- **xs:** 4px. Tiny badges and progress fragments.
- **sm:** 8px. Small buttons and compact controls.
- **md:** 12px. Default buttons, inputs, and rows.
- **lg:** 16px. Popovers, menus, list groups.
- **xl:** 24px. Cards, sheets, panels.
- **2xl:** 32px. Large dashboard modules, capture sheet, command palette.
- **full:** 9999px. Pills, avatars, status capsules.

### Shape rules

- Buttons and inputs should share the same default radius.
- Cards and panels use larger radii than controls.
- Pills are reserved for chips, route capsules, and status capsules.
- Do not mix sharp and pill-heavy elements in the same local area.
- Avoid 0px corners in core app UI unless the component is a chart, graph, or strict data table.

## Components

Component tokens in the YAML front matter define machine-readable defaults. This section explains how to use them.

### Buttons

- **Primary:** one per region. Uses `button-primary`; supports the most important next action.
- **Secondary:** supporting action. Uses `button-secondary`; never competes visually with primary.
- **Tertiary:** quiet action. Uses `button-tertiary`; suitable for toolbar and row actions.
- **Danger:** destructive action only. Uses `button-danger`; requires deliberate confirmation.

Buttons must have tactile feedback: scale to 0.98 or translate down 1px on active. Hover states shift surface tone, not glow.

### Inputs

- Use `input-default`.
- Label must sit above the input.
- Helper text may appear below.
- Error text appears below helper text and must not rely on color alone.
- Placeholder text is never the only label.
- Focus uses the primary accent as a quiet ring or tonal outline.

### Cards and panels

- Use `card-default` for grouped content.
- Use `panel-elevated` for active modules, inspectors, and detail surfaces.
- Cards exist only when elevation communicates hierarchy.
- High-density lists should use rows and negative space before more boxes.

### Lists and rows

- Use `list-row` as the baseline row token.
- Compact rows can be 56px; rich rows can be 88px.
- Selected rows use tonal primary treatment plus a clear leading indicator.
- Bulk actions appear only after selection.
- Required actions must not exist only on hover.

### Chips and tags

- Use `chip-default` for metadata, filters, relations, and compact states.
- Use `chip-active` for active filters or active protocol context.
- Do not use chips as decorative confetti.
- Monospace labels are for technical state, timestamps, IDs, or dense metadata.

### Navigation

- Use `nav-item-active` for active route capsules.
- Collapsed sidebar items must still navigate somewhere useful.
- Expandable roots show children only when expanded content is visible.
- Top-level lens labels remain stable: Now, Plan, Operate, Knowledge, Review, Domains, Settings.
- Icons use one family and one stroke weight per surface.

### Command palette

- Use `command-palette`.
- Search input is focused immediately.
- Results are grouped by action type, object, and route.
- Keyboard navigation must be visible.
- Suggested actions are allowed, but avoid fake AI magic wording.

### Capture sheet

- Use `capture-sheet`.
- One primary input appears first.
- Type detection may suggest Task, Note, Record, or Inbox Entry.
- Classification must be reversible.
- Save action is always visible.
- Advanced fields hide behind progressive disclosure.

### Empty, loading, and error states

Every major screen must support loading, empty, content, and error states.

- Empty states include a clear title, one sentence, one primary action, and optional secondary import/template action.
- Loading states use skeletons that match the final layout. No full-screen circular spinners.
- Error states explain what failed, preserve user input when possible, and offer a recovery action.

### Motion and interaction

Stitch exports static screens, but generated designs should imply these behaviors for implementation:

- Standard spring: stiffness 100, damping 20.
- Fast feedback: stiffness 180, damping 22.
- Gentle layout transition: stiffness 80, damping 24.
- Animate opacity, scale, translation, and tonal color.
- Avoid animating top, left, width, or height in web implementation.
- Respect reduced-motion settings.
- Isolate perpetual animations in small leaf components.

Recommended micro-interactions:

- Active protocol capsule has a slow breathing status dot.
- Command palette results stagger in by 40ms increments.
- Capture sheet expands from the New Entry origin when practical.
- Task completion uses a brief check transition, then settles quietly.
- Skeleton loaders shimmer very gently.

### Compose Multiplatform implementation

Translate this file into these implementation layers:

- `TajsTheme`: owns color scheme, typography, shapes, spacing, motion tokens.
- `TajsColors`: custom extension colors not covered by Material 3.
- `TajsSpacing`: stable spacing tokens.
- `TajsShapes`: app-specific shape scale.
- `TajsMotion`: duration and spring presets.
- `TajsElevation`: tonal elevation and shadow guidance.

Recommended component split:

- `ScreenRoute`: collects state, events, navigation.
- `ScreenScaffold`: owns layout structure and slots.
- `ScreenContent`: renders state.
- `ComponentBlock`: reusable visual components.
- `ComponentState`: explicit UI state model for loading, empty, content, and error.

Reusable component previews should cover light theme, dark theme, compact width, expanded width, loading state, empty state, and error state when applicable.

### Website and marketing components

Marketing pages may be more expressive than product screens, but should still use the same tokens.

- Hero layouts are asymmetric by default.
- One primary CTA maximum.
- No filler "scroll to explore" text.
- Inline image typography is allowed on website pages, not app dashboards.
- Bento sections use 4 to 6 intentional modules with no empty grid holes.
- GSAP or Framer Motion may be used only after dependency verification.
- Do not mix GSAP and Framer Motion in the same component tree.

## Do's and Don'ts

### Do

- Do use the YAML front matter as the normative source for token values.
- Do keep major Markdown sections in this exact order.
- Do prioritize one clear primary action per screen region.
- Do use tonal layering before borders or shadows.
- Do keep product screens readable, stable, and fast.
- Do preserve stable names for lenses, objects, domains, protocols, and routes.
- Do provide loading, empty, content, and error states for major screens.
- Do collapse every multi-column product layout to one column under 768px.
- Do use keyboard-visible focus and at least 48px touch targets.
- Do keep user input during errors, failed saves, and navigation.
- Do use exact copy such as "Capture a thought," "Start focus," "Review today," "Link to project," and "This protocol is active until 18:30."

### Don't

- Don't use pure black surfaces.
- Don't use neon purple gradients or outer glow buttons.
- Don't use more than one active accent per screen.
- Don't use generic three-card feature rows.
- Don't use centered dashboard hero sections.
- Don't use random floating badges.
- Don't use decorative chips without semantic value.
- Don't use hard page-wide section dividers.
- Don't use custom mouse cursors.
- Don't use circular full-screen loading spinners.
- Don't use generic avatars or fake round numbers like 99.99 percent.
- Don't use AI-copy clichés such as "Elevate," "Unlock your potential," "Seamless," "Next-gen," or "Revolutionize."
- Don't overload cards through card-in-card-in-card nesting.
- Don't overlap text and images in app screens.
- Don't hide required actions behind hover-only UI.
- Don't allow dead clicks in collapsed navigation.
- Don't invent new names for existing objects because the screen needed "more vibe."
