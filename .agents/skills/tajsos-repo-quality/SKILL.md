---
name: tajsos-repo-quality
description: TajsOS repo quality overlay. Use for pragmatic CI/test/lint/coverage improvements aligned with current repository reality.
---

# TajsOS Repo Quality Overlay

## Quality approach

- Prefer small but meaningful quality improvements over broad process churn.
- Check coverage, Codecov, CI workflows, linting, and test commands against the actual repo before
  proposing changes.
- Use detekt/ktlint guidance only if those tools are actually present/configured in this repo.

## PR and workflow hygiene

- Keep changes scoped, reviewable, and tied to a concrete problem.
- When adding a workflow or tool, document what problem it solves and how to verify it.
- Prefer updating existing workflows/configuration over introducing parallel systems.

## Validation mindset

- Confirm commands/tasks exist before documenting them.
- Avoid speculative quality gates that do not match current project tooling.
