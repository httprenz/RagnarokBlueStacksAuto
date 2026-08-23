# Ragnarok BlueStacks Auto — Android APK Project

This is a first Android/BlueStacks automation prototype built around the screenshots you provided.

## What this version does

- Runs as an Android Accessibility Service inside BlueStacks
- Floating Pause / Stop controls over the game
- Relative-coordinate taps (so small resolution changes are less fragile)
- Auto Quest heartbeat
- Auto Combat heartbeat
- HP/SP potion checks from screenshot color sampling
- Cutscene "Tap to Skip" recognition using the screenshot you provided
- Coordinate-only fallback on Android versions that do not support Accessibility screenshots

## Important limitation

This is a prototype profile. Ragnarok has many UI states, resolutions and animations, so you should watch the first tests closely. The current profile is based on the BlueStacks viewport shown in your screenshots (roughly 1540×900).

## Build the APK

You need Android Studio only for compiling the project into an APK.

1. Open Android Studio
2. **Open** this project folder
3. Let Gradle sync
4. Choose **Build > Build Bundle(s) / APK(s) > Build APK(s)**
5. The APK will normally appear under:
   `app/build/outputs/apk/debug/app-debug.apk`

Then install that APK in BlueStacks.

## BlueStacks setup

1. Install the APK in BlueStacks.
2. Open **Ragnarok Auto**.
3. Tap **Open Accessibility Settings**.
4. Enable the **Ragnarok Auto** accessibility service.
5. Return to the app.
6. Leave Auto Quest / Auto Combat / Skip Cutscenes enabled.
7. Tap **START**.
8. Switch to Ragnarok.

A small overlay appears with Pause / Stop.

## Safety while testing

Start in a harmless area and watch it for a few minutes.
Use the floating **Stop** button immediately if it taps the wrong area.

## Profile coordinates currently used

These are relative screen positions derived from your screenshots:

- Quest panel: ~12.5% X / 35.5% Y
- Auto combat: ~92.5% X / 52.5% Y
- HP potion: ~79.7% X / 43.5% Y
- SP potion: ~84.0% X / 43.5% Y
- Cutscene skip: ~89.5% X / 11.5% Y
- Dialogue advance: ~50% X / 92% Y

## Next improvements

The next revision should add:

- reliable quest-complete / accept button detection
- death/revive template
- idle-vs-combat recognition
- cooldown-aware skill rotation
- stuck detection and movement recovery
- per-resolution calibration screen
- a visual coordinate picker
