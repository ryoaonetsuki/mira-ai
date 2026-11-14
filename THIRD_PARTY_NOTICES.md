# Third-Party Notices

Mira AI stands on open-source work. The components below are used under their
own licences; full licence texts ship with the project where the licence
requires it (see `LICENSE` for the Apache License 2.0 text).

## Mika
- Repository: https://github.com/wangzan101/Mika
- License: Apache License 2.0
- Copyright: Copyright 2026 wangzan101

Ported files (adapted for Mira, wired into Mira's decision loop):
- `app/src/main/java/io/github/ryoaonetsuki/mira/automation/agent/MikaScreenReader.kt`
  (from `app/src/main/java/com/wangzan101/mika/cap/phone/a11y/ScreenReader.kt`)
- `app/src/main/java/io/github/ryoaonetsuki/mira/automation/agent/FrameGrid.kt`
  (from `core/src/main/kotlin/com/wangzan101/mika/core/visual/FrameGrid.kt` + `FrameTuning.kt`)
- `app/src/main/java/io/github/ryoaonetsuki/mira/automation/agent/MikaAccuracy.kt`
  (from `core/src/main/kotlin/com/wangzan101/mika/core/uistate/Accuracy.kt`)
- `app/src/main/java/io/github/ryoaonetsuki/mira/automation/agent/MikaPacing.kt`
  (from `core/src/main/kotlin/com/wangzan101/mika/core/cap/phone/TypingPacing.kt` + `GesturePacing.kt`)
- `app/src/main/java/io/github/ryoaonetsuki/mira/automation/agent/MikaReliableActions.kt`
  (from `app/src/main/java/com/wangzan101/mika/cap/phone/a11y/AndroidScreen.kt` reliability core)
- `app/src/main/java/io/github/ryoaonetsuki/mira/automation/agent/MikaOperatingPace.kt`
  (from `core/src/main/kotlin/com/wangzan101/mika/core/cap/phone/AppPacing.kt` + `OperatingPace.kt`)
- `app/src/main/java/io/github/ryoaonetsuki/mira/automation/agent/MiraScreenPort.kt`
  (from `core/src/main/kotlin/com/wangzan101/mika/core/uistate/ScreenPort.kt`)
- `app/src/main/java/io/github/ryoaonetsuki/mira/automation/agent/MiraAndroidScreen.kt`
  (from `app/src/main/java/com/wangzan101/mika/cap/phone/a11y/AndroidScreen.kt`, reliability
  core + ScreenPort implementation, virtual display / shell / multi-profile dropped)

NOT copied from Mika: PRoot (GPL-2.0), talloc (LGPL-3.0), Alpine Linux rootfs (mixed
licences) — those are bundled in Mika but are not free to reuse here.

## Tapilot
- Repository: https://github.com/tapilot-ai/tapilot
- License: MIT License
- Turn F design source: LoopGuard design, AccessibilityService adapter and Skills
  lifecycle (design + adapted code, with attribution).

## flutter_automate
- License: MIT License (per pub.dev)
- Gesture and node primitives adapted into Mira's automation layer
  (`GestureEngine.longClick` / `pinchIn` / `pinchOut`, `UiSelector`).

## Jarvis
- Repository: https://github.com/AbuZar-Ansarii/Jarvis
- License: MIT License
- Copyright (c) 2026 AbuZar Ansarii
- Ported (Dart → Kotlin): `TaskStep`, `Skill`, `RecoveryEngine`, and the
  accessibility-service gesture core in `MiraAccessibilityService`.

## WebRTC audio processing (vendored)
- Source: the WebRTC project, vendored under `app/src/main/cpp/webrtc-audio-processing/`
  (AEC3 echo cancellation; licence text at `webrtc/LICENSE` in that tree)
- License: BSD 3-Clause License
- Copyright (c) 2011, The WebRTC project authors. All rights reserved.
- Used for software echo cancellation only (never hardware AEC, never Speex).

## Abseil (C++)
- License: Apache License 2.0
- Dependency of the vendored WebRTC audio-processing library.

## Pipecat client
- Artifact: `ai.pipecat:client:1.2.0`
- License: BSD 2-Clause License
- Real-time voice client used by Mira Live.

## Coil
- Artifact: `io.coil-kt:coil-compose:2.7.0`
- License: Apache License 2.0
- Image loading for Compose.

## OkHttp
- Artifact: `com.squareup.okhttp3:okhttp:4.12.0`
- License: Apache License 2.0
- HTTP client for Gemini, Supabase and the image-generation API.

## AndroidX (Jetpack Compose, Room, CameraX, Lifecycle, Navigation)
- License: Apache License 2.0
- Google's Android Jetpack libraries used throughout the app.
