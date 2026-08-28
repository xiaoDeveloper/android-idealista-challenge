# Tasks: Property Browsing Experience

**Input**: `specs/002-property-browsing-experience/spec.md`, `ui-spec.md`, `plan.md`,
`research.md`, and `data-model.md`

**Inherited foundation**: `001-idealista-core` owns architecture, API endpoints,
networking states, navigation, Room favorites, `propertyCode` identity, and favorite
dates. These tasks change only media and detail presentation required by this feature.

**Organization**: Tasks are grouped by user story and ordered test-first where a
focused automated contract is practical. `[P]` is used only when files are disjoint and
the task has no dependency on unfinished work.

## Phase 1: Foundational UI data and shared media component

**Purpose**: Preserve the additional supported response data and provide one reusable
paged-media implementation without adding a dependency or changing inherited flows.

- [X] T001 [P] Add failing model and repository mapping coverage for ordered non-blank images, optional supported image tags, ignored unsupported tags, detail currency suffix, `homeType`/`extendedPropertyType`/`propertyType` precedence, typed detail facts, and valid/invalid dedicated energy grades in `app/src/test/java/com/xiao/idealistachallenge/model/PropertyModelsTest.kt`, `app/src/test/java/com/xiao/idealistachallenge/data/remote/DetailMappingTest.kt`, and `app/src/test/java/com/xiao/idealistachallenge/data/repository/AdRepositoryTest.kt`.
- [X] T002 [P] Add Spanish media-position, supported image-tag, detail hierarchy, characteristic, description expansion, and energy strings plus only the required indicator/media dimensions in `app/src/main/res/values/strings.xml` and `app/src/main/res/values/dimens.xml`, reusing existing theme colors and typography.
- [X] T003 Implement `PropertyImage`, closed image/energy enums, and the typed detail fields in `app/src/main/java/com/xiao/idealistachallenge/model/PropertyImage.kt`, `app/src/main/java/com/xiao/idealistachallenge/model/PropertyAd.kt`, and `app/src/main/java/com/xiao/idealistachallenge/model/PropertyDetails.kt`; extend `ImageDto`, detail type/operation/characteristic/energy DTOs, and bounded filtering/mapping in `app/src/main/java/com/xiao/idealistachallenge/data/remote/IdealistaDtos.kt` and `app/src/main/java/com/xiao/idealistachallenge/data/repository/AdRepository.kt` until T001 passes, without changing endpoints or selected-ID composition.
- [X] T004 Add the reusable horizontal `RecyclerView` page adapter, `PagerSnapHelper` position callback, per-page Coil success/failure handling, localized accessibility descriptions, and stable no-image page in `app/src/main/java/com/xiao/idealistachallenge/ui/media/PropertyImagePagerAdapter.kt` and `app/src/main/res/layout/item_property_image.xml`, using only existing dependencies.

**Checkpoint**: Ordered media and supported detail facts are available to both screens;
the shared pager can render success, individual failure, and no-image states.

---

## Phase 2: User Story 1 - Evaluate properties while browsing (Priority: P1) 🎯 MVP

**Goal**: Browse every listing image in-place without interfering with card navigation
or persistent favorites.

**Independent test**: A listing with multiple images pages in source order and reports
its position; single, thumbnail-only, absent, and failed images retain stable media;
swiping, tapping, and favoriting perform three independent actions.

- [X] T005 [US1] Add failing listing layout and media-selection tests for multimedia precedence, thumbnail-only fallback, no-image behavior, multi-image indicator visibility, 16:9 bounds, and a separate 48 dp favorite target in `app/src/test/java/com/xiao/idealistachallenge/ui/listing/ListingLayoutContractTest.kt` and `app/src/test/java/com/xiao/idealistachallenge/ui/listing/ListingMediaSelectionTest.kt`.
- [X] T006 [US1] Replace the single listing image region with the shared pager and overlaid position indicator while keeping the favorite control outside the swipe surface in `app/src/main/res/layout/item_listing.xml`.
- [X] T007 [US1] Integrate the shared pager into `app/src/main/java/com/xiao/idealistachallenge/ui/listing/ListingAdapter.kt`; preserve settled positions by `propertyCode`, reset removed identities, route deliberate media/card taps to the inherited detail callback, and keep favorite callbacks isolated from media gestures until T005 passes.
- [X] T008 [US1] Expand the deterministic listing data in `app/src/androidTest/java/com/xiao/idealistachallenge/TestApp.kt` for multi-image, single-image, thumbnail-only, empty, and one-broken-URL rows, then add Espresso coverage proving a horizontal pager swipe neither navigates nor toggles favorite, a subsequent deliberate media/card tap opens detail for the same row, and the adjacent favorite control still updates independently in `app/src/androidTest/java/com/xiao/idealistachallenge/PropertyBrowsingJourneyTest.kt`.

**Checkpoint**: User Story 1 is independently usable and is the suggested MVP scope.

---

## Phase 3: User Story 2 - Understand a photo-led property detail (Priority: P2)

**Goal**: Present all supported detail photos and the most useful truthful information
in a clear scan order while preserving inherited navigation and favorite behavior.

**Independent test**: The dated detail fixture renders all valid photos in order, uses
only supported Spanish image labels, omits coordinate-only location, and exposes the
required information hierarchy before lower-priority content.

- [ ] T009 [P] [US2] Add failing pure presentation tests for property type/operation translation, price currency suffix, fact ordering, `Interior`/`Exterior`, true-only lift/storage/duplex labels, community-cost frequency omission, unknown-value omission, and valid energy-grade normalization in `app/src/test/java/com/xiao/idealistachallenge/ui/detail/DetailPresentationTest.kt`.
- [ ] T010 [P] [US2] Extend failing XML/accessibility contract coverage for a 4:3 detail pager, conditional indicator, ordered section IDs, absent coordinate presentation, and independently reachable favorite control in `app/src/test/java/com/xiao/idealistachallenge/ui/detail/DetailLayoutContractTest.kt`.
- [ ] T011 [US2] Implement the resource-backed detail presentation mapper used by the Fragment in `app/src/main/java/com/xiao/idealistachallenge/ui/detail/DetailPresentation.kt`, emitting only supported Spanish labels and omitting ambiguous, false-without-meaning, raw, or unavailable values until T009 passes.
- [ ] T012 [US2] Restructure `app/src/main/res/layout/fragment_detail.xml` to place the shared 4:3 pager first, followed by property type/operation, price, truthful optional location, essential facts, favorite/date, additional characteristics, description, energy, and the inherited static-response notice; preserve loading, retry, and back behavior.
- [ ] T013 [US2] Integrate paged detail media, supported tag announcements, position updates, mapped hierarchy, empty-section omission, and unchanged selected-`propertyCode` favorite behavior in `app/src/main/java/com/xiao/idealistachallenge/ui/detail/DetailFragment.kt` until T010 passes.
- [ ] T014 [US2] Expand the deterministic fixed-detail response in `app/src/androidTest/java/com/xiao/idealistachallenge/TestApp.kt` with multiple supported, unsupported, and untagged images plus typed facts, then extend `app/src/androidTest/java/com/xiao/idealistachallenge/PropertyBrowsingJourneyTest.kt` to verify detail paging, supported versus generic announcements, independent back/favorite controls, coordinate omission, and the rendered hierarchy.

**Checkpoint**: User Stories 1 and 2 work independently on top of the inherited core
journeys.

---

## Phase 4: User Story 3 - Read complete property information comfortably (Priority: P3)

**Goal**: Keep detail facts concise while making the complete description and valid
energy classifications accessible on demand.

**Independent test**: A long multi-paragraph description starts as a six-line preview,
expands to the exact full text, collapses again, and valid consumption/emissions grades
remain understandable as text; unsupported values do not render.

- [ ] T015 [US3] Add failing expansion-state tests for short, absent, long, expanded, collapsed, and retry/re-render cases in `app/src/test/java/com/xiao/idealistachallenge/ui/detail/DetailViewModelTest.kt`, asserting that the complete original description and paragraph breaks are never mutated.
- [ ] T016 [US3] Add immutable description expansion state and a guarded toggle action to `DetailUiState.Content` and `DetailViewModel` in `app/src/main/java/com/xiao/idealistachallenge/ui/detail/DetailViewModel.kt` without moving networking or favorite persistence into the Fragment or resource-backed text formatting into the ViewModel.
- [ ] T017 [US3] Add the six-line preview, conditional 48 dp `Ver más`/`Ver menos` control, accessibility state, full-text rendering, and dedicated textual consumption/emissions rows in `app/src/main/res/layout/fragment_detail.xml` and `app/src/main/java/com/xiao/idealistachallenge/ui/detail/DetailFragment.kt` until T015 passes.
- [ ] T018 [US3] Add a long multi-paragraph description and valid consumption/emissions grades to the deterministic detail response in `app/src/androidTest/java/com/xiao/idealistachallenge/TestApp.kt`, then extend `app/src/androidTest/java/com/xiao/idealistachallenge/PropertyBrowsingJourneyTest.kt` to verify one-action expansion, exact full text, collapse without loss, focusable expansion controls, and energy information that remains complete without color.

**Checkpoint**: All three user stories are independently testable and the full feature
behavior is present.

---

## Phase 5: Accessibility and final validation

**Purpose**: Verify the complete delta without unrelated refactoring or duplicated
core documentation.

- [ ] T019 [P] Extend `app/src/test/java/com/xiao/idealistachallenge/ui/PolishAccessibilityContractTest.kt` for non-empty page/placeholder/indicator/favorite/expansion labels, decorative indicator focus exclusion, 48 dp controls, scalable text containers, and omission of raw image tags and coordinates.
- [ ] T020 Run the focused new unit tests, then `.\gradlew.bat testDebugUnitTest`, `.\gradlew.bat assembleDebug`, and `.\gradlew.bat lintDebug`; record each exact outcome and fix feature regressions only.
- [ ] T021 Run `.\gradlew.bat connectedDebugAndroidTest` on a compatible device or emulator, then manually verify TalkBack page announcements and scroll actions, keyboard/switch access, 200% font scale, middle-image failure isolation, swipe-versus-tap behavior, and favorite/date preservation; leave this task open with the exact blocker if device validation cannot run.
- [ ] T022 Review only the feature diff, confirm `plan.md`, `research.md`, and `data-model.md` still describe only this feature's delta and that no `contracts/` or `quickstart.md` was introduced, run `git diff --check`, and reconcile `specs/002-property-browsing-experience/tasks.md` completion marks only from fresh evidence.

---

## Dependencies and execution order

- T001 and T002 may run in parallel because they own disjoint test and resource files.
- T003 depends on T001. T004 depends on T002 and T003.
- User Story 1 depends on T004 and proceeds T005 → T006 → T007 → T008.
- After T004, T009 and T010 may run in parallel. User Story 2 then proceeds T011 →
  T012 → T013 → T014; T011 and T012 must not be assigned simultaneous writes to
  shared detail resources.
- User Story 3 depends on the User Story 2 detail structure and proceeds T015 → T016 →
  T017 → T018.
- T019 can be prepared after final layout/resource IDs stabilize. T020-T022 are serial
  final gates.
- Parallel workers must not modify this file. A coordinator reconciles task state after
  reviewing the shared diff and verification evidence.

## Parallel examples

- Foundation: T001 mapping/model tests and T002 resource additions have no shared files.
- User Story 2: T009 owns pure presentation tests while T010 owns XML/accessibility
  contract tests; both can be written before production changes.
- Final validation: T019 is a separate test file, but it must finish before T020 begins.

## Implementation strategy

1. Complete the shared media/data foundation without changing inherited architecture or
   dependencies.
2. Deliver User Story 1 as the MVP and validate swipe, tap, and favorite separation.
3. Add the photo-led detail hierarchy for User Story 2.
4. Add description expansion and energy text for User Story 3.
5. Finish with automated checks and real-device accessibility/gesture validation.

Stop after any story checkpoint if its independent test fails. Do not refactor unrelated
core code, replace XML Views, change endpoints, or redefine favorite identity/date
behavior while implementing this list.

## Requirements traceability

- **US1 / FR-001–FR-007 / SC-001–SC-003**: T001-T008, T019-T021
- **US2 / FR-008–FR-015 / SC-001, SC-003–SC-004**: T001-T004, T009-T014, T019-T021
- **US3 / FR-016–FR-017 / SC-005**: T015-T018, T020-T021
- **Inherited favorite/accessibility constraints / FR-018–FR-020 / SC-006**: T007-T008,
  T012-T014, T017-T022
