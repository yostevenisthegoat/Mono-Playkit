# Mono PlayKit

**Your Games. Your Tools.**

[![License: MIT](https://img.shields.io/badge/License-MIT-E8E8E8.svg)](LICENSE)
[![Build & Release APK](https://github.com/yostevenisthegoat/Mono-Playkit/actions/workflows/build-apk.yml/badge.svg)](https://github.com/yostevenisthegoat/Mono-Playkit/actions/workflows/build-apk.yml)

**Mono PlayKit** is an open-source, 100% free, ad-free, offline-first Android companion toolkit built for people who enjoy physical board games, speedcubing puzzles, card games, sports, and tabletop play.

> **Philosophy:** 100% Free. No Ads. No Accounts. No Tracking. Just Useful Tools.

---

## Download Latest APK

Direct download from the latest automated GitHub Release:

**[Download Mono-PlayKit APK](https://github.com/yostevenisthegoat/Mono-Playkit/releases/latest/download/Mono-PlayKit-v1.0.apk)**

Or browse all builds on the [Releases page](https://github.com/yostevenisthegoat/Mono-Playkit/releases).

---

## Included Utilities (30 Offline Tools across 7 Categories)

- **Scoreboard**: Universal Scoreboard (Standard, Basketball, Volleyball, Badminton, Table Tennis, Tennis, Football, Boxing, Darts 301/501, Custom) with scoped team panels, undo history, match clock, and fullscreen mode.
- **Puzzles**: Rubik's Cube Timer (WCA scrambles, inspection countdown, Ao5/Ao12/PB), 3×3 Cube Solver (interactive sticker input, parity validation, step-by-step playback), Pyraminx Solver, Skewb Solver, and CFOP/Notation Puzzle Guides.
- **Board Games**: Chess Clock (Bullet/Blitz/Rapid/Classical/Custom, Fischer increment & Delay, notation recorder, Chess960 generator), Monopoly Bank, Scrabble Score & Dictionary Helper, Ludo & Snakes Ladder Companion, Dominoes Pip Calculator, Bingo Caller, Jenga Selector, and Tangram Silhouette Guide.
- **Dice & Randomizers**: Polyhedral Dice Roller (D4–D100 + modifiers), Coin Flip, Random Number Generator, and Team Generator.
- **Card Games**: TCG Life & Poison Counter (MTG, Commander, Yu-Gi-Oh!, Lorcana, Pokémon) and Playing Card Dealer.
- **Physical Toys**: Fidget Spinner RPM & Coast Timer, Yo-Yo Trick Tracker, Kendama Trick Tracker, LEGO Brick Inventory Checklist, Model Kit Build Tracker, and Visual/Audio Metronome.
- **Timers & Counters**: Stopwatch with laps/splits, Countdown Timer, Interval / HIIT Timer, and Multi-Counter.

---

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3 (Official Mono monochromatic palette & adaptive layouts)
- **Persistence:** AndroidX Room & DataStore Preferences (with JSON backup export/import)
- **Navigation:** Navigation Compose
- **CI/CD:** GitHub Actions (`.github/workflows/build-apk.yml`) building with Java 17 and Gradle 9.3.1

---

## Building from Source

### Prerequisites
- JDK 17+
- Android SDK (compileSdk 36, minSdk 24)
- Gradle 9.3.1

### Commands

```bash
# Run unit & Robolectric tests
gradle :app:testDebugUnitTest

# Build debug APK
gradle :app:assembleDebug
```

---

## Contributing

Contributions are welcome! See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on bug reports, feature suggestions, and pull requests.

## License

Mono PlayKit is licensed under the **MIT License** — see [LICENSE](LICENSE) for full details.
