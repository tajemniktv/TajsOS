# TajsOS Website

Welcome to the TajsOS website repository! This site is built with Astro.

## 🚀 Project Overview

TajsOS is a cognitive operating system designed to curate chaos into clarity.

The `website` directory contains materials to showcase and document TajsOS.

## Development and dependency validation

Use Node.js 22.12 or newer, then run `npm ci`, `npm run check`, and `npm run build` in this directory.
Commit `package.json` and `package-lock.json` together so clean installs use the tested dependency graph.

TypeScript stays on the latest 6.x release supported by `@astrojs/check`; its peer range does not
yet include TypeScript 7. Upgrade the checker before moving to that compiler line.

The `postcss-selector-parser` override keeps Starlight's transitive CSS parser on the patched 7.1.6
line or newer ([GHSA-rj75-hqrm-r3gf](https://github.com/advisories/GHSA-rj75-hqrm-r3gf)). Remove the
override when the upstream dependency graph resolves a patched parser without it. Validate with
`npm audit` as well as the website check and production build when changing this override.

