# Quickstart Validation: Idealista Core

This guide becomes runnable after the Android implementation tasks are complete.

## Prerequisites

- Android Studio with JDK 17.
- Android SDK Platform 36 and an API 24+ emulator or device.
- Network access for the official challenge endpoints.

## Commands

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat connectedDebugAndroidTest
.\gradlew.bat lintDebug
```

## Manual acceptance scenarios

1. Launch the app and observe a Spanish loading state, then the listing content.
2. Confirm every returned list item has summary information and a favorite control.
3. Favorite one item; confirm its date appears. Open its detail; confirm the same state
   and date appear there.
4. Unfavorite it from detail; return to the list and confirm the state and date are gone.
5. Favorite it again, terminate and relaunch the app, and confirm the new date persists.
6. Use a test network failure or MockWebServer fixture to confirm friendly error and
   retry states for both remote journeys.

The API and local route details are defined in [idealista-api.md](contracts/idealista-api.md);
entities and timestamp rules are defined in [data-model.md](data-model.md).
