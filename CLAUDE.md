# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Native Android app (Kotlin, single `:app` module), package `com.mckimquyen.atomicPeriodicTable`. A periodic table reference app with element details, calculator, equation balancer, quiz, unit converter, and a VIP unlock feature gating ads.

## Required setup before any build/test

The build reads a **private secrets file** not in this repo:

```
$ATOMIC_PERIODIC_TABLE_SECRETS_FILE
# defaults to: /Users/loitran/AndroidStudioProjects/@mckimquyen/myKeyStore/com.mckimquyen.atomicPeriodicTable/ads.properties
```

It must contain (build fails fast via `GradleException` if missing/empty): `VIP_30D_KEY`, `VIP_3D_KEY`, per-buildType AdMob/AppLovin IDs (`DEBUG_*`/`RELEASE_*` prefixed: `ADMOB_BANNER_ID`, `ADMOB_INTERSTITIAL_ID`, `ADMOB_APP_OPEN_ID`, `ADMOB_REWARDED_ID`, `APPLOVIN_SDK_KEY`, `APPLOVIN_BANNER_ID`, `APPLOVIN_INTERSTITIAL_ID`, `APPLOVIN_APP_OPEN_ID`, `APPLOVIN_REWARDED_ID`), and release signing (`KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`). The source of truth for this file is the private `royt93/myKeyStore` repo. This applies to **every** build variant, not just release — unit tests and debug builds need it too.

## Build / test commands

Product flavors: `dev`, `production`. Build types: `debug`, `release`.

```bash
# Assemble
./gradlew :app:assembleDevDebug
./gradlew :app:assembleProductionRelease

# Unit tests (JVM, app/src/test)
./gradlew :app:testDevDebugUnitTest
./gradlew :app:testDevDebugUnitTest --tests "com.mckimquyen.atomicPeriodicTable.feature.vip.VipCalculatorTest"
./gradlew :app:testProductionReleaseUnitTest --tests "*.VipKeysTest"

# Instrumented tests (app/src/androidTest) — requires a connected device/emulator, debug variants only
./gradlew :app:connectedDevDebugAndroidTest
./gradlew :app:connectedDevDebugAndroidTest --tests "com.mckimquyen.atomicPeriodicTable.act.MainActVipGateTest"
```

There is no CI workflow in `.github/` (only issue templates) — tests are run locally.

## Architecture

Traditional Activity + ViewBinding app. **No DI framework** (no Hilt/Dagger/Koin), **no ViewModel/LiveData/Flow/MVVM** — state lives directly in Activities, which manually instantiate their own `*Pref` (SharedPreferences wrapper) and adapter dependencies. `act/BaseAct.kt` is the shared base class (theme via `ThemePref`, locale via `LocaleHelper`, window insets, adaptive refresh rate) — a template-method base, not a ViewModel.

Package layout under `app/src/main/java/com/mckimquyen/atomicPeriodicTable/`:
- `act/` — top-level Activities (`SplashAct`, `MainAct`, `ElementInfoAct`, `TableAct`, `CalculatorAct`, `EquationBalancerAct`, `QuizAct`, `SettingsAct`, ...), plus `act/setting/` and `act/table/` subpackages
- `adt/` — RecyclerView adapters
- `feature/vip/` — VIP entitlement feature (see below)
- `common/const/AdKeys.kt` — surfaces the VIP secret and privacy policy URL from `BuildConfig`
- `model/` — data classes + companion loaders (Element, Dictionary, Equation, Indicator, Ion, Series)
- `pref/` — thin `SharedPreferences` wrapper classes, one per concern
- `util/` — helpers (chemical equation parsing/balancing, locale, translators); `util/Applovin.kt` is fully commented-out legacy code, dead — do not extend it
- `widget/` — home-screen app widget (`ShortCommandWidget`)

### Ad SDK integration

All ad logic goes through the external dependency `com.github.royt93:AdmobApplovinWrapper` (Kotlin package `com.roy.sdkadbmob`, entry point `AdManager`), not direct AdMob/AppLovin SDK calls. `RoyApp.configureAds()` builds an `AdSdkConfig` from `BuildConfig` ad IDs and calls `AdManager.setConfig(...)`. `RoyApp.initializeAdsIfNeeded(...)` is a de-duplicated async initializer (guards against double-init, queues callbacks) — call this rather than `AdManager.initialize` directly. `SplashAct` gates navigation to `MainAct` on UMP consent (`AdManager.requestConsentInfoUpdate`) → ad init → `AdManager.initSplashScreen` (App Open ad), with a race guard against `RoyApp.isFullscreenAdShowing` and a 30s emergency fallback so the user is never stuck on the splash screen. `MainAct` gates element-detail navigation behind `AdManager.showInterstitial(...)` unless `AdManager.isVipByKeyActive()`.

Ad placements are intentionally limited (do not add new ones without checking `doc/AD.MD`): banner in `ElementInfoAct`/`SettingsAct`/`FavoritePageAct`, interstitial only on element-click in `MainAct`, rewarded only inside the VIP screen.

### VIP feature (`feature/vip/`)

Not a real purchase/subscription — no Play Billing dependency. It's a shared redeem-code + rewarded-ad unlock system:
- `VipKeys.kt` — `VIP_30D_KEY`/`VIP_3D_KEY` come from `BuildConfig` (from `ads.properties`); `lookupDays(input)` maps a redeemed key string to 30 or 3 days.
- The actual entitlement flag is **not** stored by app code — it lives in the external `AdManager` (`isVipByKeyActive()`, `getVipByKeyExpiry()`, `activateVipByKey(secret, days)`, `clearVipByKey()`). The SDK only validates against a single configured secret (`AdKeys.VIP_SECRET`, which equals `VIP_30D_KEY`) — so `VipManagementAct.redeemInputKey()` always calls `activateVip(AdKeys.VIP_SECRET, days)`, passing the *looked-up day count* rather than the user's raw key string. Don't "fix" this to pass the raw key — it's required by how the SDK secret validation works.
- `VipPrefs.kt` stores only supplementary local metadata (`granted_at_ms`, `activated_days`, `user_redeemed_once`) for UI display, separate from the real entitlement.
- `VipCalculator.kt` is pure/Android-independent (elapsed-progress %, remaining time parts) — keep it that way for JVM-only unit testing.
- Two ways to gain VIP: redeem a 30D/3D key in `VipManagementAct`, or watch a rewarded ad for 3 days.

## `doc/` directory

Contains implementation reports for past migrations/features — check before touching ads or VIP:
- `doc/AD.MD` — AdmobApplovinWrapper 1.1.5 migration report (touchpoint table of every ad slot and its trigger)
- `doc/feat.md` — architectural spec covering ad/VIP integration and other implemented features
- `doc/BUILD_OPTIMIZATION.md`, `doc/GRADLE_MIGRATION_STATUS.md`, `doc/DEPRECATED_API_FIXES*.md`, `doc/memory_leak.md` — build/perf/migration notes

## Non-Gradle sibling directories

- `store-assets/` — a fully independent Next.js/Tailwind/React app (own `package.json`, `bun.lock`) for composing App Store/Play Store screenshot mockups. Not referenced by `settings.gradle` or any `build.gradle` — don't expect Android build tooling to touch it.
- `_playStore/` — currently empty placeholder (`production/release/` with only `.DS_Store` files), presumably intended for future Play Store listing assets.
