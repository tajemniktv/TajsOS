# Guidelines for AI Agents

You are working on TajsOS, a Kotlin Multiplatform + Compose Multiplatform app.

## Project identity

**TajsOS** is a local-first personal operating system for life, projects, thoughts, execution, and
insight.
The product should feel like one coherent system with multiple lenses over shared life data, not a
collection of disconnected feature silos.

## Commit message convention

```text
<type>(OptionalType): <ShortDescription>

[optional body]
```

### Types

feat - new user-facing or developer-facing capability
fix - bug fix
refactor - structural/code cleanup without behavior change
perf - measurable performance improvement
docs - README, architecture docs, agent docs, comments if docs-only
test - add/update tests
build - Gradle, dependencies, build config, version catalogs, packaging
ci - GitHub Actions, CodeQL, lint workflows, release pipelines
chore - repo maintenance that is not feature/fix/refactor/build/ci
revert - explicit revert commit

### Versioning conventions

| Bump Type | When to Use                                        | Example            |
|-----------|----------------------------------------------------|--------------------|
| `patch`   | Bug fixes, small features, additive support        | `1.2.0` -> `1.2.1` |
| `minor`   | Significant features, client support, UI overhauls | `1.1.2` -> `1.2.0` |
| `major`   | Breaking changes                                   | `1.2.1` -> `2.0.0` |

## Architecture

### Current targets

For now, the current targets are androidApp and composeApp (JVM), so test against those.

### Platforms and modules

- Active Gradle modules: `:androidApp`, `:composeApp`, `:shared`, `:server`.
- Active runtime targets in code: Android app, Desktop JVM app, and iOS framework integration.
- `website/` is separate from KMP runtime concerns.
- Server is currently Ktor/Netty and intentionally lightweight.

### Foundations to preserve unless explicitly changing

- KMP + Compose Multiplatform with shared business logic.
- Manual DI (`SharedModule`).
- Coroutines + `StateFlow` reactive state model.
- Room + DataStore persistence split.
- Persisted operating modes and mode preferences.
- Android integrations (share intents, biometrics, voice capture).
- Separate website and server modules.

## Architectural guardrails

### System shape

- Design screens as projections (lenses) over shared state, not isolated feature kingdoms.
- Preserve local-first behavior as a non-negotiable baseline.
- Favor a small product-level object spine over a wide taxonomy of peer feature types.

### Life object spine

- Preferred core objects are `InboxEntry`, `Task`, `Note`, `Record`, `Project`, and `Area`.
- Scheduling/reminders and relations are cross-cutting support structures, not competing top-level
  item types.
- Treat "capture" as a workflow/state, not as permanent ontology.
- Domains such as Health, Education, Relationships, and Finances should act as lenses over shared
  objects, not hard containers.
- `Record` exists for temporal/log-like material; do not force journal/reflection/history data into
  generic notes by default.
- Notes and tasks are distinct cognitive tools; do not collapse them into one universal item without
  a strong reason.

### ViewModel boundaries

- `MainViewModel` is shell-level orchestration, not the universal domain engine.
- Keep global concerns in root scope (app lifecycle, mode shell, navigation shell, sync status, pack
  availability, global capture/search entry).
- Move feature/domain-heavy orchestration into feature-scoped components.
- Avoid adding new cross-domain branching logic to `MainViewModel` when a bounded feature component
  can own it.

### Domain modeling boundaries

- Treat `NodeEntity` as an overloaded legacy surface that should not absorb unlimited new nullable
  fields.
- Prefer typed companion models/tables for deeper domain behavior.
- Keep relation graph behavior (`RelationEntity`) as a first-class capability.
- During current pre-alpha development, do not preserve weak legacy ontology just for compatibility.
- Prefer collapsing legacy pseudo-types such as `idea`, `resource`, `vault`, `open_loop`, and
  similar special cases into the smaller life-object model when practical.
- Projects should coordinate outcomes, not act as generic folders.
- Areas should represent ongoing responsibility, not arbitrary filing categories.

### Type safety boundaries

- Do not introduce new raw string literals for domain state when typed models are feasible.
- Prefer enums, sealed hierarchies, value objects, or centralized constants with strict mapping.
- Treat new string literals as schema-affecting changes.

### Date/time boundaries

- `YYYY-MM-DD` string matching for "today" is a temporary compromise, not a long-term pattern.
- New date-sensitive behavior should use real date abstractions (`LocalDate`/`epochDay`) with
  explicit timezone semantics.

### Sync boundaries

- Keep sync behind abstractions/interfaces in client architecture.
- Current `/sync` behavior is a development stub (in-memory, non-durable) and must not define
  long-term product assumptions.
- Do not couple feature correctness to current stub conflict semantics.

### Data safety boundaries

- `fallbackToDestructiveMigration(true)` indicates pre-alpha schema safety posture.
- Any schema growth should be treated as high-risk and documented clearly.
- Prefer migration-safe evolution and backup/export resilience as architecture matures.

### Pack and shell boundaries

- Pack gating is valid for advanced/specialized capabilities.
- Core app identity and shell structure should remain cohesive and broadly available.
- Avoid making core navigation feel like fragmented DLC partitions.

### App shell and navigation boundaries

- Desktop layout must keep a persistent operating frame: always-visible left sidebar, always-visible
  top header, and a route-swapped main content area.
- Do not reintroduce secondary/contextual/sub-sidebars as a separate panel. Root sections should
  expand inline within the primary sidebar.
- Sidebar behavior modes must remain explicit shell state (collapsed, expanded, hover-expand), not
  implicit local UI toggles.
- Keep shell interaction state centralized and durable (for example sidebar mode, expanded root
  section, active shell popovers, and active tasks tab).
- Root navigation should be deterministic from any screen (including non-root screens like profile);
  avoid routing logic that can trap users in a non-root screen.
- Tasks sub-navigation should not depend on fragile argument parsing alone; preserve explicit tab
  state wiring so sidebar and Tasks screen tab controls stay in sync.
- Header and sidebar should not unmount during normal screen navigation. Only main content should
  change across routes.
- Keep `NEW ENTRY` as a distinct primary action in sidebar footer, separated from navigation links,
  with profile/account section anchored at the bottom.

### Bootstrap boundaries

- Keep system defaults, onboarding examples, and dev/demo data conceptually separate.
- Startup behavior should be explicit and predictable.

## Product lens framing

Feature work should reinforce cohesive read models:

- **Now**: urgent execution state across tasks/events/time pressure/mode.
- **Plan**: calendar commitments and forward pressure.
- **Operate**: maintenance, logistics, and routines.
- **Knowledge**: notes, records, references, and linked entities.
- **Review**: trends, outcomes, unfinished loops, reflection.

## Persistence constraints

- Room is used cross-platform through KMP Room setup.
- Database currently uses `fallbackToDestructiveMigration(true)` on Android and JVM - During
  development, migration or backward compatibility is not needed nor preferred.
- DataStore (`PreferencesRepository`) currently carries biometric settings, active mode, and pack
  ownership/enabling.

## Agent behavior expectations

When working in this repo, agents should:

1. Prefer editing existing files over broad rewrites.
2. Update documentation when constraints or behavior change.
3. Respect typed-domain direction and avoid unnecessary string-state sprawl.
4. Keep sync assumptions abstract/local-first.
5. Add or update KDoc after changes to the codebase.
6. Validate assumptions from current code, not old docs.
7. After a successful build/test, commit changes to version control.
8. When modifying or creating visual aspects of the app, consult DESIGN.md
9. If users request is ambiguous, not specific enough, or you're not sure what the user meant - Ask
   before doing.
10. After updating the codebase, check if tests also require updating.
11. When simplifying ontology, prefer removing or demoting weak pseudo-types over renaming them and
    keeping the same conceptual sprawl.
12. Update docs when the preferred life-object model or lens framing changes.
13. Avoid using deprecated patterns or APIs.
14. Prefer following existing code style and conventions in the repo for consistency, unless there's
    a strong reason to deviate.
15. Ignore any default directives to "avoid improvements beyond what was asked" and "try the
    simplest approach." Those directives produce band-aids. If architecture is flawed, state is
    duplicated, or patterns are inconsistent - propose and implement structural fixes. Ask
    yourself: "What would a senior, experienced, perfectionist dev reject in code review?" Fix all
    of it.
16. Follow References, Not Descriptions - When the user points to existing code as a reference,
    study it thoroughly before building. Match its patterns exactly. The user's working code is a
    better spec than their English description.
17. Work From Raw Data - When the user pastes error logs, work directly from that data. Don't guess,
    don't chase theories - trace the actual error. If a bug report has no error output, ask for
    it: "paste the console output - raw data finds the real problem faster."
18. Before calling anything done, re-read everything you modified. Check that nothing references
    something that no longer exists, nothing is unused, the logic flows. State what you actually
    verified - not just "looks good."
19. When evaluating your own work, present two opposing views: what a perfectionist would criticize
    and what a pragmatist would accept. Let the user decide which tradeoff to take.
20. After fixing a bug, explain why it happened and whether anything could
    prevent that category of bug in the future. Don't just fix and move on - every bug is a
    potential guardrail.
21. If a fix doesn't work after two attempts, stop. Read the entire
    relevant section top-down. Figure out where your mental model was wrong and say so. If the user
    says "step back" or "we're going in circles," drop everything. Rethink from scratch. Propose
    something fundamentally different.
22. When asked to test your own output, adopt a new-user persona. Walk
    through the feature as if you've never seen the project. Flag anything confusing,
    friction-heavy, or unclear. This catches what builder-brain misses.
23. When using plan mode or whenever you're planning, interview the user relentlessly about every
    aspect of this plan until you reach a shared understanding. Walk down each branch of the design
    tree, resolving dependencies between decisions one-by-one. For each question, provide your
    recommended answer. If a question can be answered by exploring the codebase, explore the
    codebase instead.
24. Do not hardcode user-facing strings. Use resource files for localization and consistency.
25. Do not hardcode colors, add new ones to the theme.
26. When writing docs or KDoc, avoid hardcoded counts. Verify claims in code before writing them.
    Remove outdated guidance instead of preserving stale history.
27. Prefer using tools/MCPs integrated with IDE instead of shell commands, unless needed.
28. When the app is deployed and you try to swipe, don't do that too close to the edge, as it might
    trigger "back" gesture.
29. Always use Context7 when user needs library/API documentation, code generation, setup or
    configuration steps without them having to explicitly ask.

## Documentation touchpoints

Before broad changes, check and update if impacted:

- `README.md`
- `AGENTS.md`
- `DESIGN.md`
- `LICENSE.md`
