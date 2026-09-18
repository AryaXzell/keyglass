# KeyGlass

KeyGlass is an open-source, iOS Human Interface Guidelines (HIG) inspired Android soft keyboard (Input Method Editor) and companion configuration application. Built entirely with Kotlin and Jetpack Compose, KeyGlass delivers a clean glass aesthetic, fluid 60/120fps spring physics, and an efficient on-device predictive typing engine with zero telemetry.

---

## Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
  - [Keyboard & Typing Engine](#keyboard--typing-engine)
  - [Gestures & Ergonomics](#gestures--ergonomics)
  - [Companion Configuration Suite](#companion-configuration-suite)
  - [Internationalization & Localization](#internationalization--localization)
  - [Privacy & Security Architecture](#privacy--security-architecture)
- [Architecture & Tech Stack](#architecture--tech-stack)
- [Build Artifacts & Architecture Matrix](#build-artifacts--architecture-matrix)
- [Nightly CI/CD Pipeline](#nightly-cicd-pipeline)
- [Building from Source](#building-from-source)
- [Installation & Setup](#installation--setup)
- [License](#license)
- [Author & Credits](#author--credits)

---

## Overview

KeyGlass addresses the common performance bottlenecks of custom themed keyboards on Android. By utilizing optimized alpha compositing and hardware-accelerated draw passes instead of expensive runtime gaussian blur shaders, KeyGlass eliminates frame drops while preserving an authentic clear glass appearance.

All predictive text, autocorrection, and dictionary lookups execute locally in device memory using classical data structures (Trie prefix trees, Levenshtein edit distance, and bigram models), ensuring absolute user privacy with zero telemetry or network transmission of keystrokes.

---

## Key Features

### Keyboard & Typing Engine

- **Predictive Suggestion Bar**: Displays contextual candidate words with quick-insertion chips.
- **Typo Correction & Fuzzy Matching**: Uses Levenshtein distance calculations to correct transposition and adjacency errors on the fly.
- **Multi-Language Dictionaries**: Bundled frequency-weighted lexicons for English and Indonesian (Bahasa Indonesia).
- **Personal Dictionary Integration**: Room database backed whitelist allowing users to register custom vocabulary, technical terminology, and slang.
- **Key Bubble Popups**: Responsive visual popups with spring animations reflecting active keystrokes.
- **Auditory & Haptic Feedback**: Configurable vibration intensity (0–100%) and mechanical click audio synthesis.

### Gestures & Ergonomics

- **Spacebar Trackpad Mode**: Long-press and drag horizontally across the spacebar to precisely position the text cursor without obscuring the text view.
- **Adjustable Cursor Sensitivity**: Fine-tune trackpad velocity scaling between 0.5x and 2.0x.
- **Long-Press Key Variations**: Access accented characters, numbers, and alternate glyphs with customizable trigger delays (250ms–700ms).
- **Smart Shift & Auto-Capitalization**: Automatically engages uppercase mode at sentence beginnings and resets appropriately.

### Companion Configuration Suite

- **HIG-Compliant UI**: Segmented controls, grouped tables, smooth navigation bars, and spring micro-interactions following iOS Human Interface Guidelines.
- **Dynamic Theming**: Light Mode, Dark Mode, or System Default with high-contrast neutral palettes.
- **Custom Accent Colors**: Instant accent swatch picker (System Blue, Indigo, Purple, Pink, Orange, Green, Mint, Slate).
- **Key Corner Radius Customization**: Real-time slider adjusting corner roundness from 0dp (sharp) to 12dp (soft pebble).
- **Typography Switching**: Toggle between bundled Inter Bold display typeface and Android System font.
- **Backup & Migration**: Export and import full configuration profiles in standard JSON format.
- **Unified Settings Search**: Real-time indexed query engine matching keywords across all settings submenus.

### Internationalization & Localization

- **Full Indonesian Translation**: Complete native Indonesian localization across all onboarding steps, configuration menus, dialogs, and error messages.
- **Full English Translation**: Standard internationalized terminology throughout the app.
- **Language Switcher**: Dedicated preference item in General Settings allowing instant switching without requiring system locale modification.

### Privacy & Security Architecture

- **Zero Telemetry**: No analytics SDKs, crash reporters, or tracking libraries are bundled. Keystrokes are never logged to disk or transmitted over the network.
- **Isolated Password Fields**: All predictive text candidate generators, autocorrect passes, and clipboard scrapers are strictly suppressed in secure password input fields (`TYPE_TEXT_VARIATION_PASSWORD`, `TYPE_TEXT_VARIATION_WEB_PASSWORD`).
- **Local SQLite / Room Persistence**: Custom dictionary entries remain entirely on the local device.

---

## Architecture & Tech Stack

```
com.aryaxzell.keyglass/
├── MainActivity.kt               # Single Activity host with Jetpack Navigation
├── ime/
│   ├── KeyGlassImeService.kt     # InputMethodService implementation
│   ├── KeyboardLayouts.kt        # QWERTY, Symbol, and Numeric layout definitions
│   └── KeyboardView.kt           # Composable keyboard UI & gesture handlers
├── data/
│   ├── datastore/                # KeyGlassSettings & PreferencesRepository (DataStore)
│   ├── db/                       # Room Database for Personal Dictionary
│   ├── engine/                   # PredictiveTextEngine, Trie, & Dictionary loaders
│   └── github/                   # GitHubApiService & Nightly update installer
└── ui/
    ├── components/               # Reusable HIG UI design system components
    ├── localization/             # LocalStrings multi-language translation matrix
    ├── navigation/               # Type-safe screen routes
    ├── screens/                  # Dashboard, Appearance, Typing, Gestures, About, Search
    └── theme/                    # HIGColorPalette, Typography, & Dimensions
```

- **Language**: Kotlin 2.x
- **UI Framework**: Jetpack Compose with Material 3 integration
- **State Architecture**: MVVM, Kotlin Coroutines, and `StateFlow`
- **Persistence**: Android Jetpack DataStore Preferences & Room Database
- **Network & Parsing**: OkHttp 4 & Moshi (used strictly for GitHub Nightly updater)

---

## Build Artifacts & Architecture Matrix

KeyGlass is compiled for multiple target Application Binary Interfaces (ABIs) to optimize binary size and device compatibility:

| Artifact Name | Target ABI | Description / Recommended Target |
| :--- | :--- | :--- |
| `KeyGlass-arm64-v8a-nightly.apk` | `arm64-v8a` | **64-bit ARM**: Recommended for 95%+ of modern Android smartphones and tablets (Android 8.0+). Smallest APK size. |
| `KeyGlass-armeabi-v7a-nightly.apk` | `armeabi-v7a` | **32-bit ARM**: For older 32-bit Android devices or legacy hardware. |
| `KeyGlass-universal-arm-nightly.apk` | `arm64-v8a`, `armeabi-v7a` | **Universal ARM**: Bundles both 64-bit and 32-bit ARM binaries in a single APK for all physical ARM devices. |
| `KeyGlass-all-arch-nightly.apk` | All (`arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`) | **Universal Complete**: Supports all architectures including Android Studio emulators and ChromeOS/x86 hardware. |

---

## Nightly CI/CD Pipeline

The GitHub Actions workflow (`.github/workflows/nightly.yml`) automatically triggers on every push to `main`/`master` and performs the following pipeline:

1. Checks out repository and initializes JDK 17 (Temurin).
2. Configures the Android SDK toolchain.
3. Compiles the complete Universal build (`KeyGlass-all-arch-nightly.apk`).
4. Executes ABI split compilation (`-PenableSplits=true`) to generate dedicated 64-bit, 32-bit, and Universal ARM binaries.
5. Calculates SHA-256 cryptographic checksums for all generated packages.
6. Publishes 4 distinct GitHub Action artifacts:
   - `keyglass-arm64-v8a`
   - `keyglass-armeabi-v7a`
   - `keyglass-universal-arm`
   - `keyglass-all-architectures`
7. Automatically updates the `nightly` GitHub Pre-Release tag with downloadable APK assets for the in-app updater.

---

## Building from Source

### Prerequisites

- **JDK**: Java Development Kit 17 or higher
- **Android SDK**: Compile SDK 36, Min SDK 26
- **Gradle**: Gradle 8.x (or system Gradle)

### Build Commands

Clone the repository:
```bash
git clone https://github.com/aryaxzell/keyglass.git
cd keyglass
```

Build standard Universal Debug APK:
```bash
gradle :app:assembleDebug
```

Build all 4 multi-architecture APK variants with ABI splitting:
```bash
gradle :app:assembleDebug -PenableSplits=true
```

Run unit and screenshot tests:
```bash
gradle :app:testDebugUnitTest
```

---

## Installation & Setup

1. Download the appropriate `.apk` file for your device architecture from [GitHub Releases](https://github.com/aryaxzell/keyglass/releases) or the Nightly Action artifacts.
2. Sideload and install the APK on your Android device:
   ```bash
   adb install -r KeyGlass-arm64-v8a-nightly.apk
   ```
3. Open **KeyGlass** from your launcher.
4. Follow the 3-step setup guide:
   - **Step 1**: Enable KeyGlass in Android System Settings (`Manage Keyboards`).
   - **Step 2**: Select KeyGlass as your Active Input Method.
   - **Step 3**: Customize themes, sound, haptic intensity, and typing behavior.

---

## License

KeyGlass is licensed under the [MIT License](LICENSE). You are free to use, modify, and distribute this software in accordance with the license terms.

---

## Author & Credits

- **Creator & Lead Developer**: [Arya Vallencia](https://github.com/aryaxzell)
- **Repository**: [https://github.com/aryaxzell/keyglass](https://github.com/aryaxzell/keyglass)
- **Issues & Feedback**: [https://github.com/aryaxzell/keyglass/issues](https://github.com/aryaxzell/keyglass/issues)
