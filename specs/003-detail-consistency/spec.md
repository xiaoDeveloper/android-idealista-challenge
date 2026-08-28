# Feature Specification: Detail Consistency

**Created**: 2026-08-28

## User scenario

### User Story 1 - View the selected property truthfully (Priority: P1)

As a person browsing properties, I want Detail to show information for the listing I
opened, so that I do not make decisions using another property's information.

**Acceptance scenarios**:

1. Given a person opens any listing, Detail shows that listing's price, operation,
   property type, images, description, size, rooms, bathrooms, and supported address.
2. Given the fixed detail response identifies a different property, none of its
   content or detail-only characteristics are shown for the selected listing.
3. Given the fixed detail response identifies the selected property, its additional
   supported characteristics may enrich the selected listing.
4. Given the selected listing is not already retained locally, Detail resolves it
   from the official listing response before rendering.
5. Favorite and unfavorite actions continue to use the selected listing's
   `propertyCode`.

## Requirements

- **FR-001**: A selected listing remains the source of truth for its core detail
  information.
- **FR-002**: The fixed detail response is used only when `adid` matches the selected
  listing identity.
- **FR-003**: Missing or unusable optional detail enrichment is omitted without
  inventing data or dynamic endpoints.
- **FR-004**: Existing navigation, image paging, description expansion, accessible
  controls, and recoverable loading/error states remain available.

## Scope

This delta changes only List-to-Detail data consistency. It does not add filtering,
sorting, endpoints, dependencies, Compose, or an architecture layer.
