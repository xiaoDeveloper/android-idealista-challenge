# Implementation Plan: Idealista Property Browsing and Favorites

**Branch**: `001-idealista-core` | **Date**: 2026-08-26 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/001-idealista-core/spec.md`

## Summary

Implement the challenge as a small single-module Kotlin Android app. XML Fragments
observe ViewModel StateFlow; repositories isolate Retrofit/OkHttp remote data and Room
favorite persistence. Manual constructor injection keeps the graph explicit and easy to
test.

## Technical Context

**Language/Version**: Kotlin with Java/Kotlin 17 compatibility

**Primary Dependencies**: AndroidX lifecycle/ViewModel, Navigation Component,
RecyclerView, ViewBinding, Retrofit, OkHttp, Kotlin serialization, Coil, Room,
Coroutines/Flow, Material Components

**Storage**: Room database containing favorite ID and epoch-millisecond timestamp

**Testing**: JUnit 4, kotlinx-coroutines-test, Turbine, MockWebServer, Room in-memory
database tests, Espresso/AndroidX instrumentation

**Target Platform**: Android API 24+, target/compile SDK 36

**Project Type**: Single-module mobile application

**Performance Goals**: Keep all network/database work off the main thread; maintain
smooth RecyclerView scrolling for the challenge response size; avoid duplicate requests
caused by view recreation.

**Constraints**: XML is mandatory for both required screens; detail endpoint is static;
no offline listing cache, pagination, Compose rewrite, Hilt, or multi-module split.

**Scale/Scope**: One app module, two primary screens, arbitrary list length within the
challenge response, one local favorite record per selected property ID.

## Constitution Check

- Challenge compliance: PASS — Kotlin, XML, two journeys, favorite date, and AI evidence
  are explicit in `spec.md`.
- Smallest clear boundary: PASS — one module, focused repositories, manual injection,
  and no speculative domain layer.
- Testable behavior: PASS — remote mapping, Room, ViewModels, and core UI flows have
  named test coverage.
- Evidence and transparency: PASS — fixed detail limitation, current-vs-target state,
  and verification commands are documented.
- AI review and safety: PASS — `.agents`, `.specify`, `AGENTS.md`, and `AI_USAGE.md`
  define the review boundary and prohibit secrets or unsupported claims.

## Project Structure

### Documentation (this feature)

```text
specs/001-idealista-core/
├── spec.md
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/idealista-api.md
├── checklists/requirements.md
└── tasks.md
```

### Source Code (repository root)

```text
app/src/main/java/com/xiao/idealistachallenge/
├── App.kt
├── data/local/
├── data/remote/
├── data/repository/
├── model/
├── ui/listing/
├── ui/detail/
└── core/

app/src/main/res/layout/
├── activity_main.xml
├── fragment_listing.xml
├── fragment_detail.xml
└── item_listing.xml

app/src/test/                         # unit, repository, mapper, ViewModel tests
app/src/androidTest/                  # Room/instrumentation and core UI journey tests
```

**Structure Decision**: Keep the generated single `:app` module and add focused data,
model, UI, and core packages only as implementation requires. `MainActivity` hosts
navigation; screen-specific ViewModels and adapters remain with their feature package.

## Interfaces and Data Flow

- `IdealistaApi.listAds()` maps the list JSON to `PropertyAd` models without assuming
  the current four-item fixture.
- `IdealistaApi.getDetails()` requests the fixed detail JSON and maps its fields to a
  detail model. `DetailViewModel` retains the route's `propertyCode` separately.
- `FavoriteRepository.observeFavorite(adId)` returns the current `Favorite?` as Flow;
  `favorite(adId, nowEpochMillis)` inserts/replaces and `unfavorite(adId)` deletes.
- `ListingViewModel` combines ads with favorite records into immutable row UI models.
  `DetailViewModel` combines the loaded detail response with the selected local ID's
  favorite state.
- The Application container constructs Retrofit, Room, repositories, and ViewModel
  factories once; no service locator is exposed to UI classes.

## Error and State Rules

Use explicit Loading, Content, Empty, and Error UI states. Map transport, parsing, and
database failures to stable user-facing Spanish messages with retry actions. Collect
flows with `repeatOnLifecycle`; never run network or database work on the main thread.
Image failures show a placeholder and preserve text content.

## Build Baseline

The first implementation task changes the template's RC/SDK 36.1 baseline to stable
AGP 9.3.1, Gradle 9.5.0, compile/target SDK 36, min SDK 24, and Java/Kotlin 17. The
foundation phase does not alter those files; this records the required first task.

## Complexity Tracking

No constitution violations are planned. Room, Navigation Component, and the remote
client each directly support a mandatory journey or an approved persistence/test goal.
