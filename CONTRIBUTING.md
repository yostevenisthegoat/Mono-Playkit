# Contributing to Mono PlayKit

Thank you for helping improve **Mono PlayKit**!

## Core Principles

1. **100% Free & Open Source** — No paywalls, subscriptions, ads, or analytics SDKs.
2. **Offline-First** — All utilities should work reliably without internet access or user accounts.
3. **Mono Design Identity** — Keep UI components aligned with the dark monochromatic Mono palette (`#080808` / `#191919` / `#FFFFFF`), reserving functional color accents strictly for utility clarity (such as opposing teams or puzzle face stickers).
4. **Companion Utilities Only** — Tools assist real-world physical games, puzzles, and toys rather than replacing them.

## Development Workflow

1. Fork the repository and create a branch from `main`.
2. Make focused changes in Kotlin / Jetpack Compose.
3. Run the local test suite and verify the debug build passes:
   ```bash
   gradle :app:testDebugUnitTest
   gradle :app:assembleDebug
   ```
4. Open a pull request describing your changes clearly.

## Reporting Issues

When filing a bug report, include:
- Steps to reproduce
- Expected vs. actual behavior
- Android device/OS version (or copy diagnostics from **Settings → Report a Problem**)
