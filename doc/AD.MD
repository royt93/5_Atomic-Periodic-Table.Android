# AD Migration Plan - AdmobApplovinWrapper 1.1.5

Source prompt: `doc/AD_PROMPT_AOS.MD`  
Reference SDK doc: `/Users/loitran/AndroidStudioProjects/@mckimquyen/@playstore/@prodution/_AdmobWrapper/doc/AD.MD`  
Target SDK: `com.github.royt93:AdmobApplovinWrapper:1.1.5`

## Current Audit

| Question | Answer | Evidence |
|---|---|---|
| Project already applies wrapper? | Yes, but old/different artifact: `com.github.royt93:AdmobWrapper:1.1.1`; migrate to `AdmobApplovinWrapper:1.1.5`. | `app/build.gradle:221` |
| Old SDK still present? | Mostly removed. Current active code imports `com.roy.sdkadbmob.AdManager`; commented AppLovin/MAX remnants remain and can be deleted after grep verification. | `MainAct.kt:695`, `ElementInfoAct.kt:311`, `util/Applovin.kt` |
| Application init path? | `RoyApp.setupAdmob()` builds `AdSdkConfig`, calls `setConfig`, `earlyInit`, `registerAppOpenAdLifecycle`, then manually initializes MobileAds/AppLovin and calls `AdManager.init`. | `RoyApp.kt:20-55` |
| Splash consent + App Open warmup? | Splash calls `AdManager.initSplashScreen(this)`, but does not call UMP consent before ad warmup. | `SplashAct.kt:27-40` |
| Banner slots? | Three existing banner hosts: Element info, Settings, Favorite settings. All use `AdManager.loadBanner` and manual resume/pause/destroy. | `ElementInfoAct.kt:167-173`, `SettingsAct.kt:258-264`, `FavoritePageAct.kt:318-324` |
| Interstitial slots? | One existing trigger: user taps an element in `MainAct`, then navigation continues inside `showInterstitial` callback. Do not add new triggers. | `MainAct.kt:199-200`, `MainAct.kt:504-515` |
| VIP screen exists? | No dedicated VIP screen or rewarded/VIP flow found. Must create mandatory VIP feature in-place as new activity/fragment entry from Settings/Main. | `rg vip/reward` found only policy strings and no active VIP UI |

SDK API summary: `AdManager.setConfig` must precede `initialize`; `initialize` handles provider init, `init`, and app-open lifecycle. `loadBanner` supports auto-managed lifecycle in current SDK, `showInterstitial` and `showRewarded` are guarded by VIP/network/AdSafety, `requestConsentInfoUpdate` handles UMP, and VIP APIs include `activateVipByKey`, `getVipByKeyExpiry`, and `clearVipByKey`. SDK `1.1.5` adds AppLovin SDK key, rewarded IDs, VIP key secret, and first-install VIP grace in the reference docs.

## Touchpoint Table

| Slot | Current file | Trigger | New API | Old/current API replaced |
|---|---|---|---|---|
| Banner | `ElementInfoAct.kt` | `setupViews()` after layout binding | `AdManager.loadBanner(..., adSize = AdManager.getAdaptiveBannerSize(this), autoManageLifecycle = true if available)` | Existing `AdManager.loadBanner`; remove manual lifecycle if SDK auto-manages |
| Banner | `SettingsAct.kt` | `setupViews()` | Same as above | Existing `AdManager.loadBanner`; remove manual lifecycle if SDK auto-manages |
| Banner | `FavoritePageAct.kt` | `setupViews()` | Same as above | Existing `AdManager.loadBanner`; remove manual lifecycle if SDK auto-manages |
| Interstitial | `MainAct.kt` | Element button click, before `ElementInfoAct` navigation | `AdManager.showInterstitial(this) { startActivity(...) }` | Keep current trigger, update API only if signature changed |
| App Open | `RoyApp.kt` + `SplashAct.kt` | Splash warmup + foreground resume | `AdManager.initialize`; `AdManager.requestConsentInfoUpdate`; `AdManager.initSplashScreen` or `awaitSplashComplete` per SDK `1.1.5` API | Manual MobileAds/AppLovin init in `RoyApp` |
| Rewarded | New VIP screen | User taps "Watch ad -> 3 days VIP" | `AdManager.loadRewarded` + `AdManager.showRewarded`; fallback `showInterstitial` only inside VIP action | `(chưa có)` |

## Step-By-Step Migration Plan

### 1. Setup Gradle and BuildConfig

Files:
- `app/build.gradle`
- `AndroidManifest.xml`
- Optional new `app/src/main/java/com/mckimquyen/atomicPeriodicTable/common/const/AdKeys.kt`

Edits:
- Replace `implementation("com.github.royt93:AdmobWrapper:1.1.1")` with `implementation("com.github.royt93:AdmobApplovinWrapper:1.1.5")`.
- Add missing BuildConfig fields in both `debug` and `release`:
  - `ADMOB_REWARDED_ID`
  - `APPLOVIN_REWARDED_ID`
  - `PRIVACY_POLICY_URL`
- Keep `IS_ENABLE_ADMOB=false`; AppLovin MAX remains active runtime.
- Keep existing AppLovin SDK key in `BuildConfig.APPLOVIN_SDK_KEY`; avoid hardcoding it in `.kt`.
- Prefer moving manifest SDK key/app ID values to placeholders later; not required for first pass if stable.
- Use `AdSafetyLimits.TEST` only in debug and default/content safety in release.

Missing user data before implementation:
- Release AppLovin rewarded ID.
- Debug/release AdMob rewarded ID if fallback must be fully configured.
- Privacy Policy URL.
- Confirm whether existing AppLovin banner/interstitial/app-open IDs are production-ready.
- Optional test device IDs and EEA consent test geography.

### 2. App Init and Consent

Files:
- `RoyApp.kt`
- `SplashAct.kt`

Edits:
- Build `AdSdkConfig` with all SDK `1.1.5` fields:
  - `isEnableAdmob`
  - `isDebug`
  - AdMob banner/interstitial/app-open/rewarded IDs
  - AppLovin banner/interstitial/app-open/rewarded IDs
  - `applovinSdkKey = BuildConfig.APPLOVIN_SDK_KEY`
  - `vipKeySecret = AdKeys.VIP_SECRET`
  - `safety = if (BuildConfig.DEBUG) AdSafetyLimits.TEST else AdSafetyLimits.CONTENT`
- Replace manual `MobileAds.initialize` / `AppLovinSdk.initializeSdk` branches with `AdManager.initialize(this) { ... }` if the published `1.1.5` artifact exposes the same API as local reference.
- Move UMP request into splash startup:
  1. `requestConsentInfoUpdate`
  2. app-open warmup/await splash complete
  3. navigate to `MainAct`
- Do not subscribe to `ProcessLifecycleOwner` manually if `initialize()` registers App Open lifecycle internally. If API requires manual registration, keep exactly one registration in `RoyApp`.

### 3. VIP UI

Files to add:
- `app/src/main/java/com/mckimquyen/atomicPeriodicTable/feature/vip/FVipManagement.kt` or `VipManagementAct.kt` if the app continues its Activity-first architecture
- `feature/vip/VipKeys.kt`
- `feature/vip/VipPrefs.kt`
- `common/const/AdKeys.kt`
- `app/src/main/res/layout/f_vip_management.xml` or `a_vip_management.xml`
- Required drawables/animations/strings

Implementation:
- Use ViewBinding; no raw `findViewById` in VIP screen.
- Encode the two plain VIP keys from `AD_PROMPT_AOS.MD` as Base64 constants; no plain key in `.kt`.
- `AdKeys.VIP_SECRET` should decode the 30-day key for `AdSdkConfig.vipKeySecret`.
- `VipKeys.lookupDays(input)` validates 30-day and 3-day entries.
- `VipPrefs` stores `grantedAtMs` and `userRedeemedAtLeastOnce`.
- UI must include all mandatory rows from the prompt: status header, activation/expiry rows, elapsed progress, countdown, key input, rewarded 3-day button, disabled purchase buttons, active VIP card, revoke all, restore disabled, privacy footer.
- Use `CountDownTimer`; cancel timer and all animators in `onDestroyView`/`onDestroy`.
- Add entry point from `SettingsAct` or `MainAct` toolbar/menu; update VIP badge in `onResume` using `AdManager.isVipByKeyActive()`.

### 4. Banner

Files:
- `ElementInfoAct.kt`
- `SettingsAct.kt`
- `FavoritePageAct.kt`
- `layout_ad_banner.xml`

Edits:
- Keep the same three banner placements only.
- Use `AdManager.getAdaptiveBannerSize(this)` where supported.
- If SDK `1.1.5` auto-manages lifecycle by default, remove `adView` fields and calls to `bannerResume`, `bannerPause`, `bannerDestroy`.
- If the artifact still requires manual lifecycle, keep current nullable `View?` pattern and verify no Activity reference is retained after destroy.
- Confirm banner container remains docked in layout and does not overlay interactive content.

### 5. Interstitial

Files:
- `MainAct.kt`

Edits:
- Keep existing `AdManager.loadInterstitial(this)` in setup if still required by SDK `1.1.5`.
- Keep only the existing element-click trigger.
- Do not add app-exit, settings-open, or repeated navigation triggers.
- Preserve callback continuation so navigation always runs whether the ad shows or is blocked.

### 6. Rewarded

Files:
- VIP screen files only.

Edits:
- Load rewarded when VIP screen opens or just before button action, depending on actual `1.1.5` API.
- On "Watch ad -> 3 days VIP":
  - call `AdManager.showRewarded(activity) { earned -> if (earned) grant 3-day VIP }`
  - if rewarded fails to show, fallback to `showInterstitial` only from this button path, then grant if fallback completes per product decision.
- `grantViaRewarded()` should call `AdManager.activateVipByKey(context, VipKeys.VIP_3D_KEY, 3)`, save `grantedAtMs`, mark user redeemed, refresh UI, and trigger animation/haptic.

## Before / After

| Area | Before | After |
|---|---|---|
| SDK artifact | `com.github.royt93:AdmobWrapper:1.1.1` | `com.github.royt93:AdmobApplovinWrapper:1.1.5` |
| Provider | AppLovin active through manual branch | AppLovin active through `AdManager.initialize` config |
| Fallback | AdMob IDs configured for 3 slots only | AdMob configured for banner/interstitial/app-open/rewarded |
| App Open | `initSplashScreen` plus manual lifecycle registration | Consent-gated splash flow plus single SDK lifecycle registration |
| Consent | Not wired before splash ad | UMP request blocks ad load until resolved |
| Banner lifecycle | Manual resume/pause/destroy in each Activity | SDK auto lifecycle if available; otherwise current nullable cleanup retained |
| Interstitial | Existing element-click trigger | Same trigger, guarded by SDK `1.1.5` safety |
| Rewarded | None | VIP-only rewarded path |
| VIP | None | Mandatory key/rewarded/grace management screen |
| Frequency cap | Older wrapper defaults | `AdSafetyLimits.TEST` in debug, safe default/content in release |
| Secrets | AppLovin key duplicated in manifest/BuildConfig, no VIP secret | Runtime config through BuildConfig/AdKeys, Base64 VIP constants |
| Policy | Partial ad policy coverage | Explicit consent, app-open, rewarded, banner, and frequency verification |

## Info Needed From User

- Privacy Policy URL.
- AppLovin rewarded ID.
- AdMob rewarded ID.
- Confirm if existing production AppLovin IDs are final:
  - banner `7c47d522e938927b`
  - interstitial `fca03a66d93f71df`
  - app-open `d0c74bfa67df7491`
- Confirm if existing production AdMob IDs are final:
  - banner `ca-app-pub-3612191981543807/1070088796`
  - interstitial `ca-app-pub-3612191981543807/9709343750`
  - app-open `ca-app-pub-3612191981543807/9715699725`
- Optional: test device IDs and EEA test config.

## Build Variants

Must pass:
- `./gradlew assembleDevDebug`
- `./gradlew assembleDevRelease`
- `./gradlew assembleProductionDebug`
- `./gradlew assembleProductionRelease`

If the release variants require signing passwords from local Gradle properties, verify those are present before starting implementation.

## Verification Checklist

- [ ] `rg "AdmobWrapper:1.1.1|AdmobWrapper"` confirms no old dependency remains except docs/history.
- [ ] `rg "ADMOB_REWARDED_ID|APPLOVIN_REWARDED_ID|PRIVACY_POLICY_URL"` confirms fields are present in debug and release.
- [ ] `rg "9fA0q7eN|eQ7@93L0f"` returns 0 matches in source `.kt`.
- [ ] `RoyApp` calls `setConfig` before `initialize`.
- [ ] Splash calls consent before app-open warmup/navigation.
- [ ] App-open lifecycle is registered exactly once.
- [ ] Banner remains only in `ElementInfoAct`, `SettingsAct`, and `FavoritePageAct`.
- [ ] Interstitial remains only on the existing element-click path.
- [ ] Rewarded is only reachable from the VIP screen button.
- [ ] VIP timer uses `CountDownTimer` and is cancelled.
- [ ] VIP animators are nullable and cancelled in cleanup.
- [ ] Privacy footer opens `BuildConfig.PRIVACY_POLICY_URL`.
- [ ] Policy checks pass: no banner overlay, no splash-only app-open violation, frequency cap configured, rewarded user-initiated only, no fake ad close/click UI.
- [ ] Manual memory review: banner host references, rewarded callback capture, Application callbacks.
- [ ] Build variants above pass.

## Implementation Order

1. Collect missing IDs/URL from user.
2. Update Gradle dependency and BuildConfig fields.
3. Update `RoyApp` config/init to SDK `1.1.5`.
4. Update `SplashAct` consent-gated app-open flow.
5. Build once to catch API mismatches.
6. Update banner call sites for adaptive size / auto lifecycle.
7. Confirm interstitial path still compiles and preserves navigation.
8. Add VIP files, strings, layout, entry point, and badge.
9. Add rewarded flow inside VIP screen only.
10. Run debug/release builds and complete policy/memory checklist.
