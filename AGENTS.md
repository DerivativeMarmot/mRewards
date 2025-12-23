# Repository Guidelines

## Project Structure & Module Organization
- Root Gradle setup with single Android app module `app`. Settings in `build.gradle.kts`, versions via `gradle/libs.versions.toml` (managed by `libs` catalog).
- Source under `app/src/main/java/com/example/rewardsrader`; Compose theme files live in `ui/theme`.
- Resources in `app/src/main/res` (values, icons, XML rules). Product docs live in `docs/` (e.g., `product_functionality.md`), tooling in `tools/`.

## Build, Test, and Development Commands
- `./gradlew assembleDebug` – compile the app and produce a debug APK.
- `./gradlew testDebugUnitTest` – run JVM unit tests.
- `./gradlew connectedDebugAndroidTest` – run instrumented UI tests on an attached/emulated device.
- `./gradlew lint` – run Android lint checks.
- Android Studio: use Compose Previews for quick UI iteration; standard Run/Debug targets use the `debug` build variant.

## Coding Style & Naming Conventions
- Kotlin + Jetpack Compose; prefer idiomatic Compose patterns (state hoisting, unidirectional data flow, small composables).
- Indent with 4 spaces; keep lines concise and readable. Favor expression-bodied functions when clear.
- Package paths mirror features; keep `MainActivity` lean and move UI/state into feature packages as they grow.
- Use Material 3 components and the shared theme (`RewardsRaderTheme`) for colors/typography. Name composables as `FeatureActionScreen`, `FeatureActionCard`, etc.
- Features are modularized; avoid oversized single files—split UI/state/data by feature as they grow.

## Testing Guidelines
- Unit tests: JUnit4 in `app/src/test/java`; name files `<Subject>Test.kt`. Aim to cover viewmodels/use-cases as they appear.
- Instrumentation/UI tests: Compose UI Test + Espresso in `app/src/androidTest/java`; name files `<Subject>AndroidTest.kt`. Use `composeTestRule` with semantics tags for stable selection.
- Keep tests deterministic; avoid network/file side effects. Use fakes for data sources when added.

## Config & Data Notes
- Card/benefit templates come from a maintained JSON/YAML config (see `docs/product_functionality.md`). Ship a bundled snapshot and allow optional URL/local overrides with validation and caching.
- Store only non-sensitive card details; if sensitive identifiers are ever added, gate them behind local encryption/biometric unlock.

## Mandatory Pre-work for Agents
- Read the full `docs/product_functionality.md` before making code changes to stay aligned with product scope.
- Read the full `docs/architecture.md` before making code changes to align with structural decisions (create/update the file if missing).
- After adding a major feature or completing a milestone, update `docs/architecture.md`.

## Commit & Pull Request Guidelines
- Commit messages: short imperative mood (“Add card config loader”, “Fix benefit progress calc”). Keep related changes together.
- PRs: include a brief summary, testing performed (`./gradlew testDebugUnitTest`, previews/attach screenshots for UI), and links to any tracking issues. Call out schema/config changes that require client refresh or migration.***
