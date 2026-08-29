# Feature Specification: Listing Discovery Controls

**Created**: 2026-08-28

## User scenario

### User Story 1 - Narrow and order listings locally (Priority: P1)

As a person browsing properties, I want to narrow listings by operation and optionally
order one operation's prices, so that I can compare relevant homes without mixing sale
and rental prices.

**Acceptance scenarios**:

1. Given listings have loaded, `Todos` is selected and shows their original source
   order.
2. Given a person selects `Venta` or `Alquiler`, only listings whose normalized
   operation is respectively `sale` or `rent` are shown in their original relative
   order.
3. Given either filtered category is active, a person may choose ascending or
   descending price order; equal prices retain their source order.
4. Given a direction is selected, switching directly between `Venta` and `Alquiler`
   retains it; selecting `Todos` clears it.
5. Given any category or sort action, results update from the already loaded data
   without another listing request. A filtered category with no matches still leaves
   the controls available.
6. Favorites, property navigation, listing cards, image paging, loading, empty,
   retry, and error behavior continue unchanged.

### User Story 2 - Filter by favorite status (Priority: P1)

As a person browsing properties, I want to filter listings to see only my saved favorites,
optionally combined with category and price ordering, so that I can easily review and
compare saved homes.

**Acceptance scenarios**:

1. Given `Solo favoritos` is enabled with `Todos`, only favorite listings are shown in
   their original source order.
2. Given `Solo favoritos` is enabled with `Venta` or `Alquiler`, only favorite listings
   for that category are shown.
3. Given `Solo favoritos` is combined with price ordering on a filtered category,
   matching favorite listings are stably sorted by price ascending or descending.
4. Given `Solo favoritos` is active, unfavoriting a visible listing immediately removes it
   reactively via the Room Flow; favoriting a matching listing immediately adds it.
5. Given `Solo favoritos` is active, switching between `Venta` and `Alquiler` preserves both
   the `Solo favoritos` toggle and any active price sort direction.
6. Given `Solo favoritos` is active with a price sort direction, selecting `Todos` clears
   the price sort direction while preserving `Solo favoritos`.
7. Given `Solo favoritos` results in zero matches, the screen remains in `Content` state
   with zero rows so discovery controls remain interactive; no network request is made.
8. Given a non-empty API listing is reduced to zero rows by discovery controls, the screen
   shows a filtered-empty message and `Limpiar filtros`; the action restores `Todos`, no
   price order, and `Solo favoritos` disabled. A genuinely empty API response continues to
   use the normal empty state.

## Requirements

- **FR-001**: `Todos`, `Venta`, and `Alquiler` are available; `Todos` is the initial
  selection.
- **FR-002**: Missing or unsupported operations remain visible only in `Todos`.
- **FR-003**: Price order is available only while `Venta` or `Alquiler` is selected;
  sale and rental prices are never numerically ordered together.
- **FR-004**: Entering a filtered category does not apply a default price order.
- **FR-005**: Returning to `Todos` restores original list order and clears any price
  direction while preserving `Solo favoritos`.
- **FR-006**: Filtering and ordering are local state transformations and do not
  replace existing listing, favorite, navigation, media, or recoverable-state flows.
- **FR-007**: `Solo favoritos` is an independent toggle that filters the visible listings
  to those with an active favorite record (`favoritedAtEpochMillis != null`).
- **FR-008**: `Solo favoritos` composes with category filtering and price sort directions.
- **FR-009**: Favorite changes from Room update the filtered view reactively without
  performing new network requests.
- **FR-010**: Switching between `Venta` and `Alquiler` preserves both `Solo favoritos`
  and any selected price sort direction.
- **FR-011**: A filtered-empty result is represented separately from an API-empty result
  while retaining `Content` so the discovery controls stay available.

## Scope

This delta adds only listing discovery controls (`Todos`, `Venta`, `Alquiler`, price sort,
and `Solo favoritos`). It keeps `ListingFragment`, XML RecyclerView/property cards, the
existing list endpoint, and Room favorite storage; a small isolated Compose control host
is permitted above the RecyclerView.
