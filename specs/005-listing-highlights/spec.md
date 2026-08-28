# Feature Specification: Listing Property Highlights

**Created**: 2026-08-28

## User scenario

### User Story 1 - View property highlights on listing cards (Priority: P1)

As a person browsing properties, I want to see key highlights on each listing card (such as exterior, air conditioning, storage room, and included parking), so that I can quickly compare property characteristics directly from the list without opening each detail view.

**Acceptance scenarios**:

1. Given a listing includes positive highlight attributes, its card displays the applicable Spanish labels in deterministic order: `Exterior`, `A/C`, `Trastero`, `Garaje incluido` separated by ` · `.
2. Given a listing has no positive highlights, the highlights view is hidden (`GONE`) and takes no vertical space.
3. Given a listing has `hasParkingSpace = true` and `isParkingSpaceIncludedInPrice = true`, `Garaje incluido` is displayed; if either flag is false or null/missing, parking is not displayed as an included highlight.
4. Given false, missing, or null highlight values, they are safely omitted and do not produce placeholder text or errors.
5. The listing card layout places content in top-to-bottom order: Media -> Price with Favorite action -> Facts (`m² · hab. · baños`) -> Highlights (`Exterior · A/C · Trastero · Garaje incluido`) -> Summary/Location -> Favorite date.
6. Existing favorites, property navigation, media paging, discovery category filtering, price sorting, and Detail presentation continue to work unchanged.

## Requirements

- **FR-001**: `PropertyAdDto` maps optional `exterior`, `features` (`hasAirConditioning`, `hasBoxRoom`), and `parkingSpace` (`hasParkingSpace`, `isParkingSpaceIncludedInPrice`).
- **FR-002**: Missing or null optional highlight fields map safely to null/omission without failing serialization.
- **FR-003**: Domain model `PropertyAd` exposes `highlights: List<PropertyHighlight>` in fixed precedence order: `EXTERIOR`, `AIR_CONDITIONING`, `STORAGE_ROOM`, `INCLUDED_PARKING`.
- **FR-004**: Included parking is emitted only when both `hasParkingSpace` and `isParkingSpaceIncludedInPrice` are strictly `true`.
- **FR-005**: Highlights are rendered using Spanish resource-backed labels: `Exterior`, `A/C`, `Trastero`, `Garaje incluido`.
- **FR-006**: When `highlights` is empty, the highlight UI component is `GONE`.
- **FR-007**: The favorite touch target remains at least 48dp and separate from the media swipe area.
- **FR-008**: Detail screens and models remain completely untouched.

## Scope

This delta adds listing-card property highlights from the listing endpoint payload. It does not modify Detail, add endpoints, introduce new dependencies, or implement floor display.
