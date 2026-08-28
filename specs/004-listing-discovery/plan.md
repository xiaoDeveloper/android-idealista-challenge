# Listing Discovery Controls Plan

## Approach

`ListingViewModel` owns immutable `ListingDiscoveryUiState` with
`ListingCategory { ALL, SALE, RENT }` and optional
`PriceSortDirection { ASCENDING, DESCENDING }`. `ListingUiState.Content` carries
that state plus rows derived from the loaded source order, favorite observations, and
one atomic selection. It filters normalized trimmed operations first, then performs a
stable `BigDecimal` price ordering only for the selected sale or rental category.

Selecting `ALL` clears sorting; sort actions while `ALL` is active are ignored.
Switching between sale and rent retains a selected direction, while retry reapplies
the current selection to newly loaded rows. A filtered zero-result view remains
content with a zero count so controls stay usable; only a genuinely empty response
uses the existing empty state. `operation` already maps from `list.json` into
`PropertyAd`, so no DTO or repository change is planned.

## UI and dependencies

`fragment_listing.xml` receives one `ComposeView` above its unchanged RecyclerView.
`ListingFragment` gives it lifecycle disposal and state/callbacks only;
`ListingDiscoveryControls` is stateless and renders category chips plus two
price-direction chips only for sale/rent. It uses existing semantic day/night colors
and Spanish strings. The adapter, cards, navigation, favorite callbacks, and media
pager remain unchanged.

Enable the Kotlin Compose compiler plugin at the current Kotlin version, Compose BOM
`2026.06.01`, Compose UI, Material 3, lifecycle runtime Compose, UI-test JUnit4, and
the debug test manifest. Do not add Activity Compose, Compose Navigation, previews,
tooling, or ViewModel Compose.

## Verification

Cover initial/source order, normalized category filtering, stable ascending/descending
ordering, sale/rent direction carry-over, `Todos` reset, local-only changes, filtered
zero results, and favorite updates in focused ViewModel tests. Add minimal Compose
semantics/callback and XML-host contract coverage. Then run focused tests,
`testDebugUnitTest`, `assembleDebug`, `lintDebug`, and `git diff --check`.
