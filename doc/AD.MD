# AD Migration Report - AdmobApplovinWrapper 1.1.5

> ⚠️ **Tài liệu lịch sử — đóng băng tại thời điểm migrate lên SDK 1.1.5.** Nhiều mô tả bên dưới
> KHÔNG còn đúng với code hiện tại (2026-09-03), giữ nguyên vì đây là báo cáo lịch sử, không phải
> spec sống. Cho trạng thái hiện tại, tin `CLAUDE.md` (mục "Ad SDK integration" + "VIP feature")
> làm nguồn sự thật, không phải file này. Sai lệch đã biết:
> - SDK hiện tại: `1.8.5` (không phải `1.1.5`) — xem `app/build.gradle`.
> - `IS_ENABLE_ADMOB=true` cho cả debug lẫn release (không còn `false`/AppLovin-mặc-định như mô tả
>   ở mục "Setup Gradle" và bảng "Before/After" bên dưới). App ID lấy qua `manifestPlaceholders`
>   từ secrets (`DEBUG_ADMOB_APP_ID`/`RELEASE_ADMOB_APP_ID`), không hardcode trong Manifest.
> - Mục "6. Rewarded" mô tả `grantViaRewarded()` gọi `activateVipByKey(context, AdKeys.VIP_SECRET, 3)`
>   — đây là hack SDK cũ **đã bị bỏ**. Code thật hiện tại (`VipManagementAct.grantRewardedVip()`)
>   dùng `AdManager.grantVipDays(context, 3)`. Đừng copy lại pattern cũ trong mục 6.
>   `redeemInputKey()` cũng không còn ép cứng key = `vipKeySecret`; key gõ tay của user đi thẳng
>   qua `AdSdkConfig.vipRedeemCodes` (SDK 1.2+).
> - `RoyApp.kt` giờ có thêm `appOpenExcludedActivities` (loại trừ `SplashAct`) và
>   `AdManager.paidEventListener` (set trong `onCreate`) — 2 field này SDK `1.1.5` không có trong
>   mô tả gốc bên dưới.

Source prompt: `doc/AD_PROMPT_AOS.MD`  
Reference SDK doc: `/Users/loitran/AndroidStudioProjects/@mckimquyen/@playstore/@prodution/_AdmobWrapper/doc/AD.MD`  
Target SDK: `com.github.royt93:AdmobApplovinWrapper:1.1.5`

Status: implemented; all requested variants passed.

## Current Audit

| Question | Answer | Evidence |
|---|---|---|
| Project already applies wrapper? | Yes, upgraded from `com.github.royt93:AdmobWrapper:1.1.1` to `com.github.royt93:AdmobApplovinWrapper:1.1.5`. | `app/build.gradle` |
| Old SDK still present? | ✅ Fully removed (2026-09-02, release audit). Current active code imports only `com.roy.sdkadbmob.AdManager`. Deleted `util/Applovin.kt` (164 lines, entirely commented-out) and the commented `createAdInter()`/`showAd()` MAX-listener blocks in `MainAct.kt` and `ElementInfoAct.kt` — grep-verified zero remaining `com.applovin.*`/`MaxAd`/`MaxError` references outside `RoyApp.kt`'s legitimate `AppLovinFullscreenActivity` lifecycle-tracking comment (that one is live code, not a remnant). | — |
| Application init path? | `RoyApp.configureAds()` builds full `AdSdkConfig` and calls `setConfig`; `initializeAdsIfNeeded()` is invoked only after Splash consent resolves. | `RoyApp.kt` |
| Splash consent + App Open warmup? | Splash now calls `requestConsentInfoUpdate`, then `RoyApp.initializeAdsIfNeeded`, then `initSplashScreen`. | `SplashAct.kt` |
| Banner slots? | Three existing banner hosts remain. All use adaptive banner size and SDK auto lifecycle. | `ElementInfoAct.kt`, `SettingsAct.kt`, `FavoritePageAct.kt` |
| Interstitial slots? | One existing trigger: user taps an element in `MainAct`, then navigation continues inside `showInterstitial` callback. Do not add new triggers. | `MainAct.kt:199-200`, `MainAct.kt:504-515` |
| VIP screen exists? | Yes. Added `VipManagementAct` with Settings and Main entry points, key activation, rewarded 3-day flow, active card, countdown, revoke, and privacy footer. | `feature/vip/VipManagementAct.kt` |

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

Configured user data:
- AppLovin rewarded ID: `<private:DEBUG_APPLOVIN_REWARDED_ID>`.
- AdMob rewarded ID: Google test rewarded ID, per user instruction.
- Privacy Policy URL: `https://loitp.notion.site/Term-Privacy-Policy-Disclaimer-319b1cd8783942fa8923d2a3c9bce60f`.
- Main provider remains AppLovin through `IS_ENABLE_ADMOB=false`.

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
- Replaced manual `MobileAds.initialize` / `AppLovinSdk.initializeSdk` branches with `AdManager.initialize(this) { ... }`, called from Splash after consent.
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
- `grantViaRewarded()` validates the 3-day flow but calls `AdManager.activateVipByKey(context, AdKeys.VIP_SECRET, 3)` because SDK `1.1.5` has a single-secret VIP design and rejects any key other than `AdSdkConfig.vipKeySecret`.

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

## User Inputs Applied

- AppLovin rewarded ID: `<private:DEBUG_APPLOVIN_REWARDED_ID>`.
- AdMob rewarded ID: `<private:DEBUG_ADMOB_REWARDED_ID>`.
- Privacy Policy URL: `https://loitp.notion.site/Term-Privacy-Policy-Disclaimer-319b1cd8783942fa8923d2a3c9bce60f`.
- VIP keys are Base64-encoded in Kotlin; plain keys are not stored in `.kt` sources.

## Build Variants

Must pass:
- `./gradlew assembleDevDebug`
- `./gradlew assembleDevRelease`
- `./gradlew assembleProductionDebug`
- `./gradlew assembleProductionRelease`

If the release variants require signing passwords from local Gradle properties, verify those are present before starting implementation.

## Verification Checklist

- [x] `rg "ADMOB_REWARDED_ID|APPLOVIN_REWARDED_ID|PRIVACY_POLICY_URL"` confirms fields are present in debug and release.
- [x] `rg "9fA0q7eN|eQ7@93L0f" app/src/main/java -g "*.kt"` returns 0 matches.
- [x] `RoyApp` calls `setConfig` before `initialize`.
- [x] Splash calls consent before app-open warmup/navigation.
- [x] App-open lifecycle is registered by SDK `initialize`.
- [x] Banner remains only in `ElementInfoAct`, `SettingsAct`, and `FavoritePageAct`.
- [x] Interstitial remains only on the existing element-click path.
- [x] Rewarded is only reachable from the VIP screen button.
- [x] VIP timer uses `CountDownTimer` and is cancelled.
- [x] VIP animators are nullable and cancelled in cleanup.
- [x] Privacy footer opens `BuildConfig.PRIVACY_POLICY_URL`.
- [x] `assembleDevDebug` passed.
- [x] `assembleDevRelease` passed.
- [x] `assembleProductionDebug` passed.
- [x] `assembleProductionRelease` passed.

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
