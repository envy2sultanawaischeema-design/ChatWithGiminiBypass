# Implementation Plan - Convert to Android Library for JitPack Publishing

Convert the `:app` module into an Android Library (`com.android.library`) with `maven-publish` configuration for JitPack publishing.

## User Requirements
1. Update `gradle/libs.versions.toml` with `kotlin` version and `android-library`, `kotlin-android` plugins.
2. Update `app/build.gradle.kts` to use `com.android.library`, remove `applicationId`, configure `maven-publish` for `com.github.envy2sultanawaischeema-design:ChatWithGiminiBypass:1.0.0`.
3. Ensure the project syncs and builds successfully as an AAR.

## Proposed Changes

### [MODIFY] [libs.versions.toml](file:///C:/Users/Awais/AndroidStudioProjects/ChatWithGiminiBypass/gradle/libs.versions.toml)
- Add `kotlin = "2.0.21"` under `[versions]`.
- Add `android-library` and `kotlin-android` under `[plugins]`.

### [MODIFY] [build.gradle.kts](file:///C:/Users/Awais/AndroidStudioProjects/ChatWithGiminiBypass/app/build.gradle.kts)
- Replace content with library configuration and `maven-publish` block.

### [NEW] [consumer-rules.pro](file:///C:/Users/Awais/AndroidStudioProjects/ChatWithGiminiBypass/app/consumer-rules.pro)
- Create empty `consumer-rules.pro` if referenced.

## Verification Plan

### Automated Tests
- Perform `gradle_sync` and `gradle_build` (`app:assembleRelease`) to ensure the library compiles and generates the `.aar` artifact.
