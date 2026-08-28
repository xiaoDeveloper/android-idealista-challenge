# Listing Property Highlights Plan

## Approach

Extend `PropertyAdDto` with nullable fields matching the official listing payload contract:
- `exterior: Boolean? = null`
- `features: FeaturesDto? = null` with `hasAirConditioning: Boolean? = null` and `hasBoxRoom: Boolean? = null`
- `parkingSpace: ParkingSpaceDto? = null` with `hasParkingSpace: Boolean? = null` and `isParkingSpaceIncludedInPrice: Boolean? = null`

Define domain enum `PropertyHighlight { EXTERIOR, AIR_CONDITIONING, STORAGE_ROOM, INCLUDED_PARKING }` and property `highlights: List<PropertyHighlight> = emptyList()` on `PropertyAd`.

In `AdRepository`, map DTOs into `PropertyAd` with deterministic highlight order:
1. `EXTERIOR` if `exterior == true`
2. `AIR_CONDITIONING` if `features?.hasAirConditioning == true`
3. `STORAGE_ROOM` if `features?.hasBoxRoom == true`
4. `INCLUDED_PARKING` if `parkingSpace?.hasParkingSpace == true && parkingSpace?.isParkingSpaceIncludedInPrice == true`

## UI and Resources

Add Spanish string resources in `strings.xml`:
- `property_air_conditioning`: `"A/C"`
- `property_parking_included`: `"Garaje incluido"`
(Reusing existing `property_exterior` and `property_storage_room`).

In `item_listing.xml`, adjust card layout hierarchy:
1. Media viewport (`listingMediaViewport`)
2. Content layout (vertical):
   - Horizontal header with `listingPrice` (weight 1) and `favoriteButton` (48dp min target)
   - `listingFacts` (`TextView`)
   - `listingHighlights` (`TextView`, gone by default)
   - `listingSummary` (`TextView`)
   - `favoriteDate` (`TextView`, gone by default)

In `ListingAdapter`, bind highlight strings formatted with `" · "` and toggle visibility (`isVisible = highlightLabels.isNotEmpty()`).

## Verification

- DTO serialization tests: decode optional fields, verify null defaults, verify ignored unknown keys.
- Official fixture mapping tests: verify parsed DTO fields across all 4 sample items in `list-observed-2026-08-27.json`.
- Repository unit tests: verify highlight inclusion, ordering, exclusion on false/null, and parking truth table.
- XML layout contract tests: verify highlights view exists, is gone by default, and is positioned between facts and summary.
- Android UI/journey tests: verify Spanish highlight text rendering and hidden state on empty highlights.
- Run `testDebugUnitTest`, `assembleDebug`, `lintDebug`, `connectedDebugAndroidTest`, and `git diff --check`.
