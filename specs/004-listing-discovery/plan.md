# Listing Discovery Controls Plan

## Approach

`ListingViewModel` owns immutable `ListingDiscoveryUiState` with
`ListingCategory { ALL, SALE, RENT }`, optional
`PriceSortDirection { ASCENDING, DESCENDING }`, and
`favoritesOnly: Boolean = false`. `ListingUiState.Content` carries
that state plus rows derived from the loaded source order, favorite observations, and
atomic discovery selections.

The local transformation pipeline applies:
1. Category filtering on normalized trimmed operations (`ALL`, `SALE`, `RENT`).
2. Favorite filtering (`favoritedAtEpochMillis != null`) when `favoritesOnly` is `true`.
3. Stable `BigDecimal` price ordering when a sort direction is active on `SALE` or `RENT`.

Selecting `ALL` clears price sorting but preserves `favoritesOnly`; sort actions while `ALL`
is active are ignored. Switching between `SALE` and `RENT` retains both selected direction
and `favoritesOnly`. Toggling `favoritesOnly` updates local derived rows without new API requests.
Favorite changes emitted by Room's Flow trigger reactive re-evaluation of derived rows
automatically. A filtered zero-result view remains content with a zero count so controls
stay usable; only a genuinely empty API response uses the empty state. `operation` already maps
from `list.json` into `PropertyAd`, and favorites are tracked in Room, so no DTO or repository
change is planned.

## UI and dependencies

`fragment_listing.xml` receives one `ComposeView` above its unchanged RecyclerView.
`ListingFragment` gives it lifecycle disposal and state/callbacks only;
`ListingDiscoveryControls` is stateless and renders category chips (`Todos`, `Venta`, `Alquiler`),
the `Solo favoritos` filter chip, and two price-direction chips only for sale/rent.
It uses existing semantic day/night colors and Spanish strings (`R.string.listing_filter_favorites_only`).
The adapter, cards, navigation, favorite callbacks, and media pager remain unchanged.

Enable the Kotlin Compose compiler plugin at the current Kotlin version, Compose BOM
`2026.06.01`, Compose UI, Material 3, lifecycle runtime Compose, UI-test JUnit4, and
the debug test manifest. Do not add Activity Compose, Compose Navigation, previews,
tooling, or ViewModel Compose.

## Verification

Cover initial/source order, normalized category filtering, stable ascending/descending
ordering, sale/rent direction carry-over, `Todos` reset with `favoritesOnly` preservation,
`Solo favoritos` filtering across all categories, reactive favorite insertion/removal,
zero-result state preservation, and single-request guarantees in focused ViewModel tests.
Add minimal Compose semantics/callback coverage for `Solo favoritos`. Then run focused tests,
`testDebugUnitTest`, `assembleDebug`, `lintDebug`, and `git diff --check`.
