# TajsOS Skills System

This folder contains generic technical skills and TajsOS-specific overlay skills.

## Priority Order

Use this precedence whenever guidance conflicts:

1. Current user/task instruction
2. Current repo reality and canonical repo docs (`AGENTS.md`, `README.md`, `ARCHITECTURE.md`,
   `DESIGN.md`)
3. TajsOS-specific overlay skills
4. Generic Compose/Android skills in this folder
5. External inspiration/examples

TajsOS overlay skills override generic skills, but they do not override current code or canonical
docs.

## Skill Inventory

### Generic/reference skills currently present

- `compose-skill`
- `app-architecture`
- `data-layer`
- `domain-layer`
- `ui-layer`
- `navigation-3`
- `edge-to-edge`
- `r8-analyzer`
- `play-billing-library-version-upgrade`
- `migrate-xml-views-to-jetpack-compose`

### TajsOS-specific overlay skills

- `tajsos-product-ux`
- `tajsos-compose-screen-system`
- `tajsos-kmp-architecture`
- `tajsos-gradle-build`
- `tajsos-repo-quality`

### External inspiration

External examples are optional and lowest priority. No external style guide should supersede TajsOS
repo truth.

Amethyst-inspired skills are not currently present in this repo tree.

## When To Use Each TajsOS Skill

| Skill                          | Use When                                                                                                                                |
|--------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| `tajsos-product-ux`            | Deciding product framing, naming, UX direction, feature focus, and coherence vs sprawl.                                                 |
| `tajsos-compose-screen-system` | Designing or reviewing Compose screen/chrome structure (`AppShell`, `ScreenScaffold`, `SplitScreenScaffold`, route/content boundaries). |
| `tajsos-kmp-architecture`      | Deciding `commonMain` vs platform placement, shared domain/repository/state boundaries, `expect/actual` usage.                          |
| `tajsos-gradle-build`          | Updating build config/dependencies, version alignment, and selecting valid verification commands.                                       |
| `tajsos-repo-quality`          | Improving CI/test/lint/coverage/PR hygiene with repo-verified, incremental quality improvements.                                        |

## Conflict Resolution Examples

- If a generic Android skill suggests an Android-specific pattern for shared business logic, keep
  shared logic in `commonMain` unless repo reality requires platform code.
- If a generic navigation example conflicts with TajsOS shell structure, keep `AppShell`/
  `ScreenScaffold` ownership and adapt navigation inside that structure.
- If a generic style recommendation conflicts with current repo implementation, follow current
  code + canonical docs and then apply TajsOS overlay rules.

## IDE Compatibility

- `.agent/skills` is the normalized local skill source used in this repo.
- Claude Code / JetBrains plugin discovers project skills from `.claude/skills`.
- If your IDE agent does not pick up skills by default, mirror `.agent/skills` to `.claude/skills`.
