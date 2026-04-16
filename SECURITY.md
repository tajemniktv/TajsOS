# Security Policy

## Supported scope

TajsOS is still experimental and moving quickly, so support promises are intentionally limited.

Security reports are welcome for:
- code execution vulnerabilities,
- credential or secret exposure,
- auth/session issues in any future remote features,
- unsafe default behavior that could expose user data,
- vulnerabilities in official builds or repository-managed code.

Please also report:
- accidental secret commits,
- insecure configuration in CI/CD,
- dependency issues that create a meaningful real-world risk.

## Please do not use public issues for sensitive vulnerabilities

If you believe you found a security issue, do **not** open a public GitHub issue first.

Instead, email:
**tajemniktv@outlook.com**

Use a subject line like:
```text
[TajsOS Security] short description
```

## What to include

Please include as much of this as you can:
- a clear description of the issue,
- affected component(s),
- reproduction steps,
- proof of concept if relevant,
- impact assessment,
- suggested fix if you have one,
- whether the issue is already public anywhere.

## Response expectations

This is a solo-maintained project, so response times may vary.
That said, good-faith reports will be reviewed as reasonably quickly as possible.

## Disclosure expectations

Please give the maintainer a reasonable chance to investigate and patch before public disclosure.

Coordinated disclosure is appreciated.
Dumping a vuln publicly without giving any chance to respond is a dick move.

## Out of scope

The following are generally out of scope unless they create a concrete exploit path:
- purely theoretical issues without a plausible attack path,
- low-impact missing headers on future web surfaces,
- best-practice complaints without real risk,
- issues only affecting unsupported forks or unofficial builds,
- bugs caused solely by local device compromise.

## Dependencies

If the issue is primarily in a third-party dependency or platform, please still report it if TajsOS is affected in practice.
