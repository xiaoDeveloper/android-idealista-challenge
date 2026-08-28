# Feature Specification: Detail Media Enrichment

**Created**: 2026-08-29

## User scenario

### User Story 1 - View richer media for the selected property (Priority: P1)

As a person viewing a property, I want Detail to use the complete image collection
from the fixed detail response only when it belongs to the listing I opened, so that I
can assess that property with richer media without seeing another property's photos.

**Acceptance scenarios**:

1. Given the fixed detail response `adid` equals the selected listing's `propertyCode`,
   Detail uses every valid detail image in response order after duplicate URLs are
   removed; the committed official fixture therefore presents its ten-image collection.
2. Given the fixed detail response identifies a different listing, Detail retains the
   selected listing snapshot media and does not apply any detail media or metadata.
3. Given detail loading fails, or a matching response has no valid image URL, Detail
   retains the selected listing snapshot media.
4. Given matching detail images repeat a URL, the first non-blank trimmed occurrence
   remains, later occurrences are omitted, and the remaining image order is stable.
5. Given a matching detail image has a supported category, Detail shows its Spanish
   category together with its multi-image position, such as `Salón · 1 / 10` or
   `Zonas comunes · 9 / 10`.
6. Given a category is unsupported or missing, a non-blank localized name may label the
   image; when neither is usable, Detail shows only the existing position. Single-image
   indicator behavior, placeholders, image paging, favorites, and loading/error states
   remain unchanged.

## Requirements

- **FR-001**: Detail media enrichment MUST remain gated by
  `detail.adid.toString() == selectedAd.propertyCode`.
- **FR-002**: `localizedName` MUST map with each detail image URL and tag.
- **FR-003**: A matching detail response with one or more valid images MUST replace the
  selected listing snapshot media with its complete valid detail collection.
- **FR-004**: Valid detail URLs MUST be trimmed, deduplicated by exact case-sensitive
  URL, and retained in first-occurrence source order.
- **FR-005**: A mismatch, detail-request failure, or matching response with no valid
  detail images MUST leave the listing snapshot media unchanged.
- **FR-006**: Supported image semantics MUST include `communalareas`, presented as
  `Zonas comunes`; known semantic tags take precedence over localized names.
- **FR-007**: An unknown semantic tag MAY use a non-blank trimmed localized name; absent
  or unusable metadata MUST not prevent image rendering or position presentation.
- **FR-008**: Multi-image Detail indicators MUST present an available category/name with
  the current position; unlabeled images retain the existing position-only format.

## Scope

This delta enriches only Detail media through the existing DTO, `PropertyImage`,
repository enrichment, shared pager, Detail UI, and Spanish resources. It does not add
endpoints, navigation, persistence, dependencies, a new gallery, or changes to listing
cards, favorites, or unrelated Detail content.
