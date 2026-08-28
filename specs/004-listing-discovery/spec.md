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

## Requirements

- **FR-001**: `Todos`, `Venta`, and `Alquiler` are available; `Todos` is the initial
  selection.
- **FR-002**: Missing or unsupported operations remain visible only in `Todos`.
- **FR-003**: Price order is available only while `Venta` or `Alquiler` is selected;
  sale and rental prices are never numerically ordered together.
- **FR-004**: Entering a filtered category does not apply a default price order.
- **FR-005**: Returning to `Todos` restores original list order and clears any price
  direction.
- **FR-006**: Filtering and ordering are local state transformations and do not
  replace existing listing, favorite, navigation, media, or recoverable-state flows.

## Scope

This delta adds only listing discovery controls. It keeps `ListingFragment`, XML
RecyclerView/property cards, the existing list endpoint, and the current operation
mapping; a small isolated Compose control host is permitted above the RecyclerView.
