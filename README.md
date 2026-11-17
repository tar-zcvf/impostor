# Masked

<p align="center">
  <img src="app/src/main/playstore/icon_512.png" alt="Masked app icon" width="96" height="96" />
</p>

<p align="center">
  <strong>Guess the word. Catch the imposter.</strong><br/>
  A fully offline, pass-and-play social-deduction word game for Android.
</p>

<p align="center">
  <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-2.0.21-7F52FF" />
  <img alt="Jetpack Compose" src="https://img.shields.io/badge/Jetpack%20Compose-2025.01.00-4285F4" />
  <img alt="AGP" src="https://img.shields.io/badge/Android%20Gradle%20Plugin-8.7.3-green" />
  <img alt="minSdk" src="https://img.shields.io/badge/minSdk-26%20(Android%208.0%2B)-green" />
</p>

<p align="center">
  <img src="app/src/main/playstore/feature_graphic_1024x500.png" alt="Masked feature graphic" width="600" />
</p>

## Overview

Masked is a party word game for 3–8 players on a single device. Every player secretly sees the **same word** — except one **imposter**, who sees a *different* word from the same category. Players take turns describing their word (without saying it), then vote out the person they believe is the imposter.

- The imposter is drawn **once per match** and stays the same across all rounds, so their score accumulates — a deliberate twist on the classic rule where the imposter is re-drawn every round.
- If the team ejects the imposter, everyone scores a point; if the imposter survives the vote, *they* score.
- Play as many rounds as you like (1–10) and the final scoreboard settles it.

**Privacy first:** Masked is fully offline. No accounts, no ads, no tracking, no network access. Player names and best scores are stored locally on the device with Jetpack DataStore.

## Features

- **Pass-and-play for 3–8 players** on one phone
- **7 word categories**, 112 distinct words (Animals, Food & Drink, Nature, Vehicles, Places, Everyday Things, Jobs)
- **Rounds 1–10** with an optional **turn timer** (up to 120 s, or no limit)
- **Persistent high scores** and remembered player list (offline, on-device)
- **Share scores** to the clipboard at the end of a match
- **Material 3 UI** built entirely with Jetpack Compose
- **Zero third-party runtime dependencies** for game logic — pure Kotlin

## How to play

1. **Setup** — add 3–8 players (the first defaults to *You*), pick a category or *All categories*, set the number of rounds and the turn timer.
2. **Word reveal** — pass the phone around; each player secretly taps to reveal *their* word.
3. **Describe** — one speaking turn at a time. Describe your word without saying it (the timer keeps everyone honest).
4. **Vote** — each player secretly votes for the person they think is the imposter.
5. **Results** — the word and the imposter are revealed; points go to the team or the imposter.
6. Repeat for as many rounds as you set, then see the final scoreboard.

## Tech stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.0.21 (JVM target 17) |
| UI | Jetpack Compose (BOM 2025.01.00), Material 3 |
| Architecture | MVVM — single immutable `StateFlow<UiState>` state machine |
| State | `MutableStateFlow` + `copy()` updates; screens are pure functions of state |
| Persistence | Jetpack DataStore Preferences (last players, best scores) |
| Concurrency | Kotlin coroutines / Flow |
| Build | Gradle 8.13, AGP 8.7.3, Compose compiler plugin 2.0.21 |
| Release | R8 minification + resource shrinking, v2 APK signing |
| Tests | JUnit 4, Mockito, Robolectric 4.14 (Compose UI on JVM), instrumented smoke test |

## Architecture

The whole match is driven by one ViewModel over an explicit phase state machine:

```
SETUP → WORD_REVEAL → DESCRIBE → VOTING → ROUND_RESULT → (next round | GAME_OVER)
```

All game state lives in an immutable `UiState` data class exposed as `StateFlow<UiState>`; every Compose screen renders from it. The core game logic (`Match`, `WordBank`) is **pure Kotlin with no Android imports**, so the entire rules engine is unit-testable on the JVM:

- `Match` — owns the imposter, vote tallying, and score books; rejects invalid setup (fewer than 3 players, duplicate names, zero rounds).
- `WordBank` — 7 categories × 16 words, with invariants enforced at load time (no duplicates, ≥ 4 words/category) and a safe `drawRound()` that guarantees the imposter word differs from the shared word.
- `GameRepository` — minimal DataStore-backed persistence (recent players, per-player best totals) with defensive encode/parse.

### Project structure

```
impostor/
├── app/
│   ├── build.gradle.kts            # module config + release signing
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml # single-activity app
│       │   ├── java/com/maskedparty/game/
│       │   │   ├── MaskedApp.kt             # Application; wires GameRepository
│       │   │   ├── MainActivity.kt          # single Compose activity
│       │   │   ├── GameViewModel.kt         # UiState state machine
│       │   │   ├── data/
│       │   │   │   ├── Word.kt              # WordCategory, WordBank, RoundWords
│       │   │   │   └── GameRepository.kt    # DataStore persistence
│       │   │   ├── game/
│       │   │   │   └── Match.kt             # imposter, votes, scoring (pure Kotlin)
│       │   │   └── ui/
│       │   │       ├── theme/MaskedTheme.kt
│       │   │       ├── components/Components.kt
│       │   │       └── screens/             # Root, Setup, Game, Match screens
│       │   ├── playstore/                   # Play Store icon + feature graphic
│       │   └── res/                         # launcher icons, strings, themes
│       ├── test/                            # JVM: unit + Robolectric Compose tests
│       └── androidTest/                     # instrumented smoke test
├── keystore/                              # release keystore (git-ignored)
├── scripts/
│   ├── build_release.sh                 # tests → APK → AAB → test APK, verifies sigs
│   └── gen_assets.py                    # generates Play Store artwork
├── settings.gradle.kts
├── build.gradle.kts
└── gradle/wrapper/                      # Gradle 8.13 wrapper
```

## Getting started

**Prerequisites:** JDK 17, Android SDK (platform 35, build-tools 35.0.0), Gradle 8.13 (wrapper included).

```bash
# Debug build
./gradlew :app:assembleDebug

# JVM test suite (35 tests: game logic, view model, codec, Robolectric Compose UI)
./gradlew :app:testDebugUnitTest

# Instrumented smoke test (needs a connected device or emulator)
./gradlew :app:connectedDebugAndroidTest
```

### Release builds

The release APK and App Bundle are signed automatically when a keystore is present:

1. Create the keystore (once) — **keep this file safe; it is the only key that can sign updates**:

   ```bash
   keytool -genkeypair -v \
     -keystore keystore/masked-release.jks \
     -alias masked -keyalg RSA -keysize 2048 -validity 10000
   ```

2. Create `keystore/keystore.properties` (both files are git-ignored):

   ```properties
   storeFile=../keystore/masked-release.jks
   storePassword=<store password>
   keyAlias=masked
   keyPassword=<key password>
   ```

3. Build:

   ```bash
   ./gradlew :app:assembleRelease :app:bundleRelease
   # or the full pipeline:
   ./scripts/build_release.sh
   ```

   Artifacts: `app/build/outputs/apk/release/app-release.apk` (sideload) and `app/build/outputs/bundle/release/app-release.aab` (Play Store).

   If no keystore is found, the build succeeds **unsigned** and logs a warning, so CI can sign separately.

## Testing

| Suite | Tests | Runs on |
|---|---|---|
| `MatchTest` | 9 | JVM |
| `GameViewModelTest` | 13 | JVM |
| `WordBankTest` | 6 | JVM |
| `RepositoryCodecTest` | 6 | JVM |
| `MainActivityComposeTest` (Robolectric Compose) | 1 | JVM |
| **Total** | **35** | |

Plus a Compose instrumented smoke test (`MainActivityInstrumentedTest`) that boots the real UI and verifies the setup screen — requires a device or emulator.

## Privacy

Masked makes **no network requests**. There are no accounts, ads, analytics, or third-party SDKs. The only data stored is your player list and best scores, in a local DataStore file you can clear from *Settings → App info → Storage*.

## Contributing

Issues and pull requests are welcome. Good first contributions: new word categories, localization (`res/values-*`), or extra test coverage for `Match` edge cases.

## License

All rights reserved. This repository is not licensed for reuse; contact the maintainer for commercial use.

---

<p align="center">
  <sub>© 2026 Masked. Built with Kotlin, Jetpack Compose & Material 3.</sub>
</p>
