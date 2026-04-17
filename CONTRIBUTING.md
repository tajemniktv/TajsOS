# Contributing to TajsOS

Thanks for wanting to contribute.

TajsOS is a source-available, local-first personal operating system for life, projects, thoughts,
execution, and insight. It is still highly experimental. That means contributions are welcome, but
they need to be intentional, focused, and aligned with the product vision rather than “generic
productivity app” drift.

Before contributing, please read:

- `README.md`
- `LICENSE.md`
- `DESIGN.md`
- `AGENTS.md`
- `ARCHITECTURE.md`

## First: understand what TajsOS is trying to be

TajsOS is not trying to become a random bucket of disconnected features.
It aims to be:

- local-first,
- cohesive,
- fast to operate,
- useful for neurodivergent brains without becoming caricatured “ADHD software”,
- visually deliberate rather than bland,
- opinionated enough to feel like a system, not a pile of screens.

Current product focus:

- Android
- Desktop
- coherent foundations over feature spam
- deeply polished core flows over shallow breadth

## What kinds of contributions are most useful

High-value contributions usually look like this:

- focused bug fixes
- performance improvements
- accessibility fixes that preserve the intended design language
- cleanup/refactors that reduce complexity without changing behavior
- tests for fragile logic
- documentation improvements that match reality
- UX polish that sharpens existing flows
- carefully argued feature work that fits the product direction

Lower-value contributions usually look like this:

- broad rewrites with unclear payoff
- framework churn for its own sake
- “more features” that fragment the product
- changes that flatten the visual identity into generic Material sludge
- speculative abstractions that make the code harder to reason about
- docs that describe an imaginary project instead of the actual repo

## Before opening a big PR

For anything non-trivial, please open an issue first and explain:

- the problem,
- why it matters,
- the proposed approach,
- tradeoffs,
- what parts of the app it touches.

This is especially important for:

- architecture changes,
- new core object types,
- navigation changes,
- persistence changes,
- sync/server changes,
- design system changes,
- anything that meaningfully affects product scope.

## Ground rules for contributors

### 1. Preserve coherence

TajsOS should feel like one system.
Prefer improving the existing model over adding parallel concepts.

### 2. Keep diffs focused

Small and reviewable beats sprawling and clever.
One pull request should generally solve one class of problem.

### 3. Prefer reality over theory

Read the code before proposing structural changes.
Do not assume docs are current just because they exist.

### 4. Do not fight the product vision

If a change pushes TajsOS toward “Notion clone”, “generic task app”, or “dashboard full of widgets
because widgets are cool”, it is probably the wrong direction.

### 5. Respect the local-first model

Do not introduce remote-first assumptions into features that should work locally.

### 6. Respect the design language

The app is intentionally trying to feel tactile, high-end, and system-like.
Do not sand it down into generic enterprise UI.

### 7. Document meaningful change

If your change affects behavior, setup, scope, architecture, or workflow, update the relevant docs
in the same PR.

## Branching and pull request expectations

Use a dedicated branch for your work.

Recommended branch naming:

- `feat/...`
- `fix/...`
- `docs/...`
- `refactor/...`
- `perf/...`
- `test/...`
- `chore/...`

Recommended commit format:

```text
<type>: <description>
```

Examples:

```text
feat: add note detail split layout
fix: prevent duplicate today pin creation
refactor: extract shared screen scaffold
docs: update architecture boundaries
```

## Coding expectations

### Kotlin / Compose

- Prefer readable, boring, maintainable code over “smart” code.
- Preserve platform boundaries.
- Keep business logic out of UI where possible.
- Avoid introducing heavy dependencies without a strong reason.
- Follow existing patterns unless there is a clear reason to improve them.

### Architecture

- Respect module boundaries.
- Prefer editing existing files over creating unnecessary new layers.
- Do not invent abstractions before they earn their keep.
- If a refactor changes boundaries, explain why in the PR.

### UI / UX

- Keep interactions clear and low-friction.
- Optimize for flow, not ornament.
- Make empty, loading, and error states intentional.
- Avoid adding options, toggles, or settings unless they solve a real problem.

### Performance

- Be mindful of startup cost, recomposition churn, database overhead, and unnecessary allocations.
- “Looks cleaner” is not enough if it makes core flows slower or less stable.

## Design expectations

Read `DESIGN.md` before touching core UI.

In short:

- preserve the “system / cockpit / tactile” feel,
- avoid generic flatness,
- avoid noisy clutter,
- avoid accidental visual inconsistency,
- do not add UI chrome without purpose,
- prefer depth, hierarchy, and restraint.

If you change reusable UI patterns, include screenshots or screen recordings in the PR.

## Documentation expectations

Keep these current when relevant:

- `README.md`
- `AGENTS.md`
- `ARCHITECTURE.md`
- `DESIGN.md`
- `ROADMAP.md`
- `CHANGELOG.md`

If you changed behavior and did not update docs, the PR is probably incomplete.

## Testing

Please test what you touch.

At minimum, include:

- what you changed,
- how you verified it,
- which platforms you checked,
- known limitations.

Useful verification examples:

- Android emulator / device tested
- Desktop JVM tested
- screenshots attached
- reproduction steps before/after
- unit tests added for logic changes

### Code Coverage

TajsOS uses [Kover](https://github.com/Kotlin/kotlinx-kover) for code coverage. Coverage is
automatically tracked and reported via [Codecov](https://codecov.io/gh/tajemniktv/TajsOS).

#### Run coverage locally

To generate a local coverage report, run:

```bash
./gradlew koverHtmlReport
```

This will generate an HTML report in:
`build/reports/kover/html/index.html`

You can also generate an XML report (used by Codecov) with:

```bash
./gradlew koverXmlReport
```

#### Coverage boundaries

- **Shared logic**: Target high coverage in `shared/` as it contains the core business logic.
- **UI logic**: `composeApp/` coverage is expected but may be limited by platform-specific UI
  rendering.
- **Server**: Tracked separately within the same report.
- **Android**: JVM unit tests in `androidApp/` are covered; instrumentation tests are currently not.

#### Codecov in PRs

Codecov will post a comment on your PR with a coverage summary and status checks. A small drop in
coverage is allowed to avoid blocking reviews, but significant regressions should be justified.

## PR checklist

Before opening a pull request, make sure you have:

- read the relevant docs,
- kept the diff focused,
- tested the change,
- updated docs if needed,
- explained tradeoffs,
- included screenshots for UI changes,
- avoided unrelated cleanup,
- ensured the change aligns with the product direction.

## Licensing and legal note

TajsOS is source-available, not open-source in the usual permissive sense.
By contributing, you agree that your contribution may be used, modified, relicensed, and distributed
by the project owner as described in `LICENSE.md`.

Also note:

- forks are allowed for review / issue investigation / pull request preparation,
- unofficial public distributions are not allowed unless explicitly permitted,
- official branding and releases remain reserved.

If that does not work for you, please do not contribute code.

## AI-generated contributions

AI assistance is allowed, but not as an excuse for sloppy work.

If you use AI:

- review everything,
- verify claims against the actual repo,
- do not submit hallucinated architecture,
- do not dump giant machine-generated rewrites,
- make sure the final result is yours, intentional, and maintainable.

Low-signal AI sludge will be closed.

## Questions

If you are unsure whether something fits TajsOS, open an issue first.
That is much better than spending hours on a PR that was never going to fit.

Thanks for caring enough to build on the project.
