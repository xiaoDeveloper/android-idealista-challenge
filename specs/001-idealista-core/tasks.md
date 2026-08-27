# Tasks: Idealista Property Browsing and Favorites

**Input**: Design documents from `/specs/001-idealista-core/`

**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, and
`contracts/idealista-api.md`

**Organization**: Tasks are grouped by user story. Tests are explicit because this
feature is approved to include meaningful automated coverage.

## Phase 1: Setup

- [X] T001 Update `gradle/libs.versions.toml` and `app/build.gradle.kts` to stable AGP 9.3.1-compatible dependencies, compile/target SDK 36, min SDK 24, and Java/Kotlin 17.
- [X] T002 Enable ViewBinding and add the approved Lifecycle, Navigation, RecyclerView, Retrofit, OkHttp, Kotlin serialization, Coil, Room, and Coroutines dependencies in `app/build.gradle.kts`.
- [X] T003 [P] Add `INTERNET` permission and launcher `MainActivity` declaration in `app/src/main/AndroidManifest.xml`.
- [X] T004 [P] Add Spanish strings, theme tokens, and accessible content descriptions in `app/src/main/res/values/strings.xml` and related resources.

## Phase 2: Foundational

- [X] T005 [P] Create immutable app models in `app/src/main/java/com/xiao/idealistachallenge/model/PropertyAd.kt`, `PropertyDetails.kt`, and `Favorite.kt` from `data-model.md`.
- [X] T006 Correct Retrofit DTOs, decimal serializer configuration, and `IdealistaApi` support in `app/src/main/java/com/xiao/idealistachallenge/data/remote/` according to the verified nested-price and decimal-size contract.
- [X] T007 [P] Create Room entity, DAO, database, and favorite repository in `app/src/main/java/com/xiao/idealistachallenge/data/local/` and `data/repository/FavoriteRepository.kt`.
- [X] T008 Create the Application dependency container and ViewModel factories in `app/src/main/java/com/xiao/idealistachallenge/core/` without introducing a service locator or DI framework.
- [X] T009 Create `MainActivity`, navigation graph, and shared error/date helpers in `app/src/main/java/com/xiao/idealistachallenge/` and `app/src/main/res/navigation/nav_graph.xml`.

## Phase 3: User Story 1 - Browse property listings (P1) 🎯 MVP

**Independent test**: A dated, observed official list fixture is served locally and
every returned ad is rendered; empty, error, retry, and favorite-row states are covered
without the detail screen.

- [X] T010 [US1] Add `app/src/test/resources/fixtures/idealista/list-observed-2026-08-27.json` from the documented official observation, then update MockWebServer mapping/error tests in `app/src/test/java/com/xiao/idealistachallenge/data/remote/IdealistaApiMappingTest.kt` to assert the nested price and decimal-size wire structure offline.
- [X] T011 [US1] Update listing repository tests in `app/src/test/java/com/xiao/idealistachallenge/data/repository/AdRepositoryTest.kt` for nested-price precedence, top-level fallback without nested suffix, exact-integral size normalization, invalid optional size omission, empty response, malformed payload, and transport error.
- [X] T012 [US1] Adapt ViewModel StateFlow test DTO helpers in `app/src/test/java/com/xiao/idealistachallenge/ui/listing/ListingViewModelTest.kt` to the corrected wire DTO shape and revalidate loading, content, empty, error, retry, and favorite-date projection.
- [X] T013 Implement verified list repository normalization in `app/src/main/java/com/xiao/idealistachallenge/data/repository/AdRepository.kt`: preserve nested amount/suffix pairs, use top-level price only as a suffix-free fallback, and normalize optional decimal size to `Int?` only when exact and valid.
- [X] T014 Implement `ListingViewModel` and immutable row UI models in `app/src/main/java/com/xiao/idealistachallenge/ui/listing/ListingViewModel.kt`.
- [X] T015 [P] [US1] Create listing card and screen XML in `app/src/main/res/layout/item_listing.xml` and `fragment_listing.xml` with loading, empty, error, retry, and accessible favorite controls.
- [X] T016 Implement `ListingAdapter` and `ListingFragment` in `app/src/main/java/com/xiao/idealistachallenge/ui/listing/`, collecting state with `repeatOnLifecycle`.

## Phase 4: User Story 2 - Open property detail (P1)

**Independent test**: A selected `propertyCode` reaches a separate detail destination,
the fixed detail response is rendered, and back navigation returns to the list.

- [X] T017 [P] [US2] Write detail mapping and fixed-response tests in `app/src/test/java/com/xiao/idealistachallenge/data/remote/DetailMappingTest.kt`, using the same observed-fixture provenance approach when the detail wire contract is verified.
- [X] T018 [P] [US2] Write `DetailViewModel` loading, success, error, retry, and selected-ID context tests in `app/src/test/java/com/xiao/idealistachallenge/ui/detail/DetailViewModelTest.kt`.
- [X] T019 Implement detail repository mapping and selected-ID composition in `app/src/main/java/com/xiao/idealistachallenge/data/repository/AdRepository.kt` and `ui/detail/DetailViewModel.kt`.
- [ ] T020 [P] [US2] Create `fragment_detail.xml` with Spanish labels, image placeholder, loading, error, retry, and accessible favorite/date controls.
- [ ] T021 Implement `DetailFragment` navigation argument handling and lifecycle-aware state collection in `app/src/main/java/com/xiao/idealistachallenge/ui/detail/DetailFragment.kt`.
- [ ] T022 [US2] Wire listing item selection to the detail destination and document the static endpoint behavior in the navigation graph and UI copy.

## Phase 5: User Story 3 - Manage persistent favorites (P1)

**Independent test**: Room-backed favorite insert, delete, timestamp replacement,
process recreation, and synchronized list/detail state all pass.

- [ ] T023 [P] [US3] Write Room DAO tests for insert, observe, delete, and replacement timestamp behavior in `app/src/test/java/com/xiao/idealistachallenge/data/local/FavoriteDaoTest.kt`.
- [ ] T024 [P] [US3] Write favorite repository tests proving a new timestamp on re-favorite and removal on unfavorite in `app/src/test/java/com/xiao/idealistachallenge/data/repository/FavoriteRepositoryTest.kt`.
- [ ] T025 [P] [US3] Add core Espresso journey coverage for list favorite, detail favorite, date display, and back-stack synchronization in `app/src/androidTest/java/com/xiao/idealistachallenge/FavoriteJourneyTest.kt`.
- [ ] T026 Implement favorite actions and date formatting at the UI boundary in both `ListingViewModel` and `DetailViewModel`.
- [ ] T027 [US3] Connect Room Flow updates to both screen states and ensure process recreation restores the selected ID's favorite record.

## Phase 6: Polish and cross-cutting validation

- [ ] T028 [P] Add missing-image, long-description, content-description, touch-target, and Spanish string coverage in relevant unit/instrumentation tests and resources.
- [ ] T029 [P] Update `docs/architecture/ARCHITECTURE.md`, `README.md`, and `docs/ai/AI_USAGE.md` from proposed to verified statements only after implementation evidence exists.
- [ ] T030 Run `quickstart.md`, `.\gradlew.bat assembleDebug`, `.\gradlew.bat testDebugUnitTest`, `.\gradlew.bat connectedDebugAndroidTest` when a device is available, and `.\gradlew.bat lintDebug`; fix failures before release review.

## Dependencies and execution order

- Phase 1 precedes all app code; T001 must complete before dependency compilation.
- Phase 2 blocks all user stories.
- US1 and US2 can be developed in parallel after foundational infrastructure, but US2
  consumes the navigation host from T009 and the selected ID convention from T005.
- US3 consumes the favorite repository from T007 and the screen state models from US1/US2.
- Phase 6 follows all desired user stories.
- Corrective test-first exception: T010 is permitted before reopened Phase 2 task T006
  only to add the dated fixture and demonstrate the expected RED mapping failure. T006
  then re-establishes the Phase 2 gate before T011-T013 proceed.
- Corrective Phase 3 order is T010 (expected RED against the old DTO) -> T006 -> T011
  -> T013 -> T012 -> focused tests, full unit tests, debug assembly, and a connected
  device live-list smoke test. T014-T016 remain complete but Phase 3 is not complete
  until the reopened tasks have fresh evidence.
- T017 and T019 remain pending; their shared remote DTO usage must follow the corrected
  fixture/provenance strategy when Phase 4 begins.

## Implementation strategy

Deliver US1 as the MVP after the foundation checkpoint, then add detail navigation and
finally persistent favorites. Write each test before its implementation, run the
smallest relevant test target, and review the diff at every checkpoint.

## Requirements traceability

- FR-001, FR-002, FR-008, and FR-009: T010-T016 and T028.
- FR-003 and FR-004: T017-T022.
- FR-005, FR-006, and FR-007: T023-T027.
- FR-010: T029 and T030.
- SC-001 and SC-005: T010-T016 and T030.
- SC-002: T017-T022 and T025.
- SC-003 and SC-004: T023-T027.
- SC-006: T004, T015, T020, and T028.
