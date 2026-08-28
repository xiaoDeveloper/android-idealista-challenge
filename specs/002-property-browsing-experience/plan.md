# Implementation Plan: Property Browsing Experience

**Branch**: `master` | **Date**: 2026-08-28 | **Spec**: [spec.md](spec.md)

## Summary

Extend the existing XML listing and fixed-detail screens with horizontally paged
property images, supported image semantics, a clearer typed detail hierarchy,
expandable description, and textual energy information. This is a presentation and
mapping delta only; all core architecture and runtime behavior remain inherited.

## Inherited baseline

The following are unchanged and are defined by `001-idealista-core`:

- Architecture, dependency injection, module boundaries, and state ownership:
  [../001-idealista-core/plan.md](../001-idealista-core/plan.md)
- Official list and fixed-detail endpoint behavior, error handling, and response
  provenance: [../001-idealista-core/research.md](../001-idealista-core/research.md)
- Navigation, `propertyCode` selected identity, Room persistence, favorite state, and
  favorite-created date: [../001-idealista-core/plan.md](../001-idealista-core/plan.md)
- Existing entities and invariants not changed below:
  [../001-idealista-core/data-model.md](../001-idealista-core/data-model.md)

This feature does not add an endpoint, route, database table, repository, module,
dependency, or public interface.

## Technical delta

**Language and platform**: Inherited Kotlin/JVM 17, Android API 24+, single `:app`
module, XML Views, Fragments, ViewBinding, and immutable ViewModel state.

**Existing dependencies used**: RecyclerView with `PagerSnapHelper` for horizontal
page snapping, and Coil for per-page image loading. ViewPager2 and new image libraries
are not added.

**Remote mapping**:

- Extend `ImageDto` to decode optional `tag`; map non-blank URLs in source order to
  `PropertyImage` and map only the six supported tags to a closed semantic value.
- Extend the fixed-detail DTO for already observed property type/operation,
  `moreCharacteristics`, and nested energy fields. Map only meaningful supported
  values into typed optional `PropertyDetails` fields; never send raw keys or unknown
  values to the UI.
- Keep the fixed `detail.json` request parameterless and retain the route's selected
  `propertyCode` as `selectedAdId` exactly as the core feature does.

**UI composition**:

- Add one reusable horizontal media adapter and page layout under `ui/media/`; both
  listing and detail own their surrounding viewport and position indicator.
- Listing-card page position is remembered by `propertyCode` inside the listing
  adapter. Media-page taps reuse the card's existing detail callback; horizontal drags
  are consumed by the pager and favorites remain a separate control.
- Detail presentation is derived through a resource-backed mapper so translations,
  ordering, and omission rules remain testable outside Fragment binding code.
- Description expansion is immutable detail UI state owned by `DetailViewModel`; the
  original description is never truncated or rewritten in the model.

**Testing**: Extend existing JUnit mapping/model/layout tests and the deterministic
instrumentation `TestApp`; add focused presentation and Espresso journey coverage.
Final gates remain `testDebugUnitTest`, `assembleDebug`, `lintDebug`, and
`connectedDebugAndroidTest` when a compatible device is available.

## Constitution check

### Before design

- **Challenge compliance**: PASS — Kotlin, XML listing/detail journeys, navigation,
  favorites, and favorite dates are preserved.
- **Smallest clear boundary**: PASS — one reusable media adapter and one presentation
  mapper have current reuse/testability value; there is no new module or dependency.
- **Testable behavior**: PASS — remote normalization and UI state remain outside Views
  and receive focused unit/instrumentation tasks.
- **Evidence**: PASS — new external-input assumptions are limited to the dated observed
  fixtures and recorded in [research.md](research.md).
- **AI review**: PASS — tasks require scoped diff, lifecycle, gesture, accessibility,
  dependency, and device-evidence review.

### After design

PASS with no exceptions. The data-model delta is typed and bounded, no inherited
contract is redefined, and all new user-visible behavior maps to executable tasks.

## Files in scope

```text
app/src/main/java/com/xiao/idealistachallenge/
├── data/remote/IdealistaDtos.kt
├── data/repository/AdRepository.kt
├── model/PropertyAd.kt
├── model/PropertyDetails.kt
├── model/PropertyImage.kt
├── ui/media/PropertyImagePagerAdapter.kt
├── ui/listing/ListingAdapter.kt
└── ui/detail/{DetailPresentation.kt,DetailViewModel.kt,DetailFragment.kt}

app/src/main/res/
├── layout/{item_property_image.xml,item_listing.xml,fragment_detail.xml}
└── values/{strings.xml,dimens.xml}

app/src/test/ and app/src/androidTest/
└── focused mapping, presentation, layout, accessibility, and journey tests
```

The project structure remains the inherited single-module layout. Exact file-by-file
execution order is canonical in [tasks.md](tasks.md).

## Artifact decision

- [research.md](research.md) records only newly consumed dated payload facts.
- [data-model.md](data-model.md) records only the material `PropertyImage` and typed
  `PropertyDetails` extensions.
- No `contracts/` is created because no external or navigation interface changes.
- No `quickstart.md` is created because validation is already concrete in `tasks.md`
  and the core harness commands are unchanged.
