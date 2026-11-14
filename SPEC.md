# Mira AI — Build spec (current, v1.0.2)

## Status
- **v1.0.2 in progress (Fix Round 4)** — post-v1.0.1 full audit fixes (4 BLOCKER · 53 MAJOR · 69 MINOR = 126 findings; audit report `~/workspace/mira-aider/post-v101-full-audit-report.md`).
- v1.0.1 delivered 2026-11-14: WhatsApp auto-reply flood fix + command interrupt/queue + mishearing fixes. v1.0.0 (pre-release) + v1.0.1 (latest) stay untouched.
- **His phone test is always the verdict** — code-verified + CI-green is never phone-proven.

## Identity
- App name: **Mira AI**. Repo: https://github.com/ryoaonetsuki/mira-ai (PUBLIC; renamed 2026-11-14 from mira-ai-voice-assistant).
- Package: `io.github.ryoaonetsuki.mira`. Developer: **Salman Biswas** — https://github.com/ryoaonetsuki.
- Licence: Apache License 2.0. See `LICENSE`, `THIRD_PARTY_NOTICES.md`.
- Stack: native Kotlin + Jetpack Compose, single module, Android only, minSdk 26 / targetSdk 36 / compileSdk 36, AGP 8.11.1, Kotlin 2.2.20, Gradle 8.14.3.
- **Delivery is a renamed debug APK** (`mira-ai-debug.apk` → `mira-ai-release.apk`). Do NOT switch to a true signed release build — it would force an uninstall and wipe on-device-only chats/skills (audit BU-M2 constraint).

## What Mira does
- **Mira Live voice** — native speech-to-speech (gemini-3.8-live, GA 15 Sep 2026), barge-in, one voice, one call, never stops or restarts while work runs. BYOK Gemini key, encrypted on-device. Direct route only (no relay).
- **Two personas, 30 voices** — Mira (female, default, voice Aoede) + Orion (male, default voice Charon); 30-voice picker with previews.
- **Phone control by voice** — open apps, tap, type, scroll, install, search, multi-step tasks in any app. Reads the whole screen (accessibility tree + vision), understands each button, re-reads the screen after every action, finishes only when the goal state is visible.
- **Skills & macros** — do a task once and it becomes a skill; next time she replays it directly. Recorded macros, learned skills, admin macros. Skills toggle controls AUTOMATIC skills only; manual tap-to-Run still works when OFF (documented in the Skills screen).
- **Text chat** — persistent on-device history (Room, phone only, never on a server), grouped by date, Think / Search / Image modes, streaming display. Chat model ladder: gemini-3.8-flash → 3.5-flash-lite.
- **Vision** — camera + ask; answers are TEXT ONLY, no voice.
- **Memory** — what she learns stays on the phone only. Called plain "Memory" in the UI, never "Yaad".
- **Websites** — builds multi-page static websites, saves them, serves on localhost in her own browser.
- **Image generation** — FLUX.1-schnell via Cloudflare Workers AI (`mira-image-api`), ONLY when the chat Image button is ON, using the FULL message text as the prompt.
- **Modes** — Friendly / Romantic / Intense + custom instruction (secret settings).
- **Backend = Supabase** (project "mira", ref `zvnfwlqxdbommtozugzt`, ap-southeast-1): licence OFF by default, admin kill-switch / maintenance / forced update / banner / push, macros as JSON. Admin panel: https://mira-admin-8qg.pages.dev/ (source not in this repo). Licence gate is wired through `LicenceGate.resolveLicenceGate` (Splash must never inline its own logic).

## Standing rules (do not break)
- **TTS allowed in EXACTLY ONE place — the voice PREVIEW (Settings voice picker). NOWHERE else.** Vision is text-only. Live voice never mixes with any TTS path.
- **Replies in ONE language only** (Settings primary), default BANGLA; UI stays English; never a translation underneath. Code in a separate copyable box.
- **Identity on-demand only.** She may say "AI agent" — never Gemini/LLM/Google/made-by-Google, never claim human. Developer = "আমার ডেভেলপার সালমান বিশ্বাস" / "আমাকে সালমান বিশ্বাস ডেভেলপ করেছে" + embedded "GitHub" anchor link (never a bare URL, no other links).
- **Dead-code rule:** anything that does not work must be REMOVED. Every option must be REAL and working — no placeholders, no fake content.
- **Chat answers ALWAYS call the model** — no canned/no-model path. Speed NORMAL (Search ON → search, Thinking ON → think). The ONLY acceptable speed fix is streaming display.
- **Phone control is VOICE MODE ONLY; chat must NOT say she can't.**
- **Conversation/session memory stays ON THE PHONE ONLY** (local Room DB) — never sent to any server database.
- **Repo language mix stays as-is** — C++ ~52% is the vendored WebRTC audio-processing library, not app code.
- **WebRTC AEC3 only** for echo cancellation; never hardware AEC, never Speex.
- **Maya is proprietary — never call any Maya/Hunter endpoint from Mira** (device-ban risk). Mika (Apache-2.0) is the automation copy source; Tapilot (MIT), flutter_automate (MIT), Jarvis (MIT) are copy sources with attribution. NEVER copy AGPL/GPL/Personal-Use code (Automator, Panda/Blurr).
- **Audit first, report findings, then fix automatically** from NEW documentation only — never build from old docs.

## Build / push / release
- GitHub Actions workflow `.github/workflows/build-apk.yml`: checkout, Java 17, Gradle assembleDebug + full unit-test suite (612 tests), 16 KB native-alignment check, upload `app-debug` artifact. **GitHub Actions is the reliable build path** — local VM APK builds do not work (SDK partial, no compileSdk 36).
- Push ONLY changed files with `tools/push.py --only=<comma-separated paths>` (never a full push — it stalls; never `push.py --help`, it EXECUTES a push). Local tree is dirty by design.
- APK verify after CI: size + md5 + ZIP entries + 16 KB `.so` alignment via LOCAL-header parse (data offset = header_offset + 30 + local filename_len + local extra_len — never the central-directory extra length).
- Release: full GitHub Release (prerelease=false), APK attached via the **browser UI** (API asset upload 401s). Never touch old releases.
- Coding lanes: MAIN Kilo (Nemotron 3.5 Super VL via Aider, free, 200 req/hour/IP) + Antigravity; BACKUP NVIDIA + AgentRouter ($125). Use whichever works; never stall. **Muse does NOT write app code — the coding agent builds; Muse prompts, verifies, and finishes only small mechanical fixes directly, disclosed.**

## Known open items (not blockers for v1.0.2)
- B2 live connect still unproven on a real phone — his phone test is the verdict.
- BU-M18 admin push cannot be delivered (zero FCM anywhere; admin panel source is not in this repo).
- ScreenPort ref-based methods (checkScreen / clearText / screenshotPng / frameGrid / swipe / gesture / waitFor / typeText / tapRef) are implemented + tested but mostly uncalled from production flows — dedicated wiring refactor, not a small fix.
- Remaining MINORs from the post-v1.0.1 audit not covered in Fix Round 4 (see the audit report).
