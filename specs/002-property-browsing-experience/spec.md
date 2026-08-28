# Feature Specification: Property Browsing Experience

**Feature Branch**: `master`

**Created**: 2026-08-28

**Status**: Draft

**Input**: Improve how people visually evaluate property listings and understand property details, using only information supported by the official Idealista Android Challenge responses.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Evaluate properties while browsing (Priority: P1)

As a person looking for a home, I want to browse the available photos directly in a listing card so that I can compare properties visually before opening one.

**Why this priority**: Listing comparison is the first decision point. Seeing the available media without leaving the list makes each card more useful while preserving the core browsing journey.

**Independent Test**: A reviewer can use a listing with multiple valid image URLs, move through the photos in their server-provided order, and still use the separate favorite and detail actions correctly.

**Acceptance Scenarios**:

1. **Given** a listing has multiple valid `multimedia.images` URLs, **When** a user swipes horizontally in its media area, **Then** the card shows each available photo in response order and communicates the current position and total, such as `1 / 7`.
2. **Given** a listing has one valid image, **When** its card is displayed, **Then** it shows that image without a multi-image position indicator or paging affordance.
3. **Given** a listing has no valid multimedia image URL but has a valid thumbnail, **When** its card is displayed, **Then** the thumbnail is the single displayed image without duplicating it in a sequence.
4. **Given** a listing has no valid image URL, **When** its card is displayed, **Then** an accessible placeholder occupies the normal media area and the card layout remains usable.
5. **Given** one displayed image fails to load, **When** the user reaches its position, **Then** an accessible placeholder replaces only that image position and the other photos, card content, favorite action, and position total remain available.
6. **Given** the user begins or completes a horizontal media swipe, **When** the gesture ends, **Then** it does not open the detail screen or change favorite state.
7. **Given** a user has interacted with the media sequence, **When** they subsequently tap the card outside the favorite control without dragging, **Then** the existing detail journey opens for the same selected listing identity.
8. **Given** a listing card is visible, **When** the user activates its favorite control, **Then** only the inherited favorite behavior changes and media paging does not interfere with that control.

---

### User Story 2 - Understand a photo-led property detail (Priority: P2)

As a person comparing a property, I want to browse its photos and scan the most useful facts in a clear order so that I can understand the property without deciphering raw source data.

**Why this priority**: The detail journey is where visual evaluation becomes a decision. A larger media area and clear information hierarchy make the fixed challenge detail response easier to understand.

**Independent Test**: With a supported detail response, a reviewer can move through all valid photos, identify the main property facts before the long description, and continue to use back navigation and the inherited favorite action.

**Acceptance Scenarios**:

1. **Given** detail content has multiple valid image URLs, **When** a user swipes the larger photo area, **Then** every valid image is reachable in server order and a lightweight current-position indicator is shown.
2. **Given** detail content has one valid image, **When** it is displayed, **Then** no multi-image affordance is shown.
3. **Given** detail content has no valid image URL or a displayed image fails to load, **When** the corresponding photo position is shown, **Then** an accessible placeholder preserves the media area and the rest of the detail remains usable.
4. **Given** an image has a supported semantic tag, **When** that tag is communicated to the user, **Then** it uses its Spanish label rather than a raw source value.
5. **Given** an image tag is missing or unsupported, **When** that image is shown or announced, **Then** no raw tag is exposed and generic photo-position information is used instead.
6. **Given** detail content is visible, **When** a user scans from top to bottom, **Then** photos are followed by property type and operation, price, truthful location when available, essential facts, favorite state/date, additional characteristics, description, and energy certification when valid.
7. **Given** photo paging is available, **When** a user activates back navigation or the favorite control near the top of the detail, **Then** the requested action is performed independently of photo paging.

---

### User Story 3 - Read complete property information comfortably (Priority: P3)

As a person deciding whether to keep considering a property, I want concise facts and a readable full description so that I can understand the important details without an overwhelming wall of text.

**Why this priority**: Rich detail is valuable after visual comparison, but it must remain calm, scannable, and complete rather than decorative or lossy.

**Independent Test**: A reviewer can inspect supported facts, expand a long description, recover the complete original text, collapse it again, and verify that unknown or ambiguous values are not presented as facts.

**Acceptance Scenarios**:

1. **Given** supported size, rooms, bathrooms, floor, interior/exterior, or lift information is available, **When** detail content is displayed, **Then** the most useful facts are presented concisely and in user-facing Spanish.
2. **Given** a supported characteristic is absent, false without a clear display meaning, unknown, low-value, or ambiguous, **When** detail content is displayed, **Then** that characteristic is omitted rather than rendered as noise or guessed text.
3. **Given** a long property description is available, **When** detail first opens, **Then** a readable preview is shown with an explicit `Ver más` action.
4. **Given** a user expands the description, **When** they activate `Ver más`, **Then** the complete original description becomes available with meaningful paragraph breaks preserved where supplied and an explicit `Ver menos` action.
5. **Given** valid energy consumption and emissions classifications are available, **When** detail content is displayed, **Then** a dedicated, understandable text section presents both classifications without relying on color alone.
6. **Given** location data is only coordinates or cannot truthfully be associated with the fixed detail response, **When** detail content is displayed, **Then** raw coordinates and fabricated address information are omitted.

### Edge Cases

- The listing response can contain zero, one, or many properties, and each property can contain zero, one, or many multimedia entries; the feature does not encode the currently observed image counts as limits.
- The media sequence consists of non-blank multimedia URLs in source order. A thumbnail is a single-image fallback only when that sequence is empty; it is not appended to a non-empty sequence.
- A URL that fails to load still occupies its valid source position, so the indicator stays truthful and the user can continue to adjacent photos.
- Image tags are optional metadata. Only `livingRoom`, `bedroom`, `kitchen`, `bathroom`, `facade`, and `corridor` may be translated to `Salón`, `Dormitorio`, `Cocina`, `Baño`, `Fachada`, and `Pasillo`; every other or missing tag is omitted.
- The current detail endpoint always returns the same remote object. The selected listing's `propertyCode` remains the inherited local favorite identity and is not a supported remote detail parameter or proof that unrelated list context can be merged into the fixed response.
- A human-readable location is shown only when it is supplied by a truthfully associated source. Coordinates alone are not a user-facing location.
- `exterior=false` has the supported, meaningful presentation `Interior`; other false values are omitted unless a later verified contract establishes an equally clear user-facing meaning.
- Community costs may be shown only as the supported amount; their frequency is not inferred. Invalid energy classifications are omitted rather than visualized.
- Loading, retry, empty, remote failure, and persistent-favorite behavior remain those inherited from the core feature and must not expose raw failure text.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The product MUST treat valid listing multimedia URLs as the preferred property-image sequence and preserve their server-provided order.
- **FR-002**: The product MUST use a valid listing thumbnail only as a single-image fallback when no valid listing multimedia URL exists.
- **FR-003**: The product MUST allow horizontal browsing of listing images when more than one valid image exists and communicate the current position and total.
- **FR-004**: The product MUST not show a carousel indicator or paging affordance for a single displayed listing image or a listing placeholder.
- **FR-005**: The product MUST retain a stable, accessible media area for absent or individually failed listing images without removing the property card or its text.
- **FR-006**: The product MUST distinguish a horizontal media drag from a deliberate card tap so that media browsing alone never opens detail or changes favorite state.
- **FR-007**: The product MUST retain the inherited card-to-detail action after media interaction and retain the inherited independent favorite action and saved-date behavior.
- **FR-008**: The product MUST allow horizontal browsing of valid detail images in server order, using a larger photo area than the listing card and a position indicator only when more than one image exists.
- **FR-009**: The product MUST preserve the detail layout and top-level navigation and favorite controls when detail images are absent or fail individually.
- **FR-010**: The product MUST expose only supported Spanish image-tag labels and MUST omit missing or unsupported tag values from visible and assistive text.
- **FR-011**: The product MUST use a generic localized photo-position description when no supported image-tag label is available, and placeholders MUST remain understandable to assistive technologies.
- **FR-012**: The product MUST present property type and operation, price, truthful location when available, and essential facts before lower-priority characteristics and description.
- **FR-013**: The product MUST translate supported values into user-facing Spanish and MUST NOT display raw source field names or technical values.
- **FR-014**: The product MUST show only meaningful, unambiguous supported characteristics and MUST NOT invent units, periodicity, meaning, or absent values.
- **FR-015**: The product MUST present valid energy consumption and emissions classifications in a dedicated text section that remains understandable without color.
- **FR-016**: The product MUST show a readable initial description preview when a description is long and provide explicit `Ver más` and `Ver menos` controls that keep the full original description available.
- **FR-017**: The product MUST preserve meaningful source paragraph breaks in the full description where available.
- **FR-018**: The product MUST retain inherited favorite state, accessible favorite labels, and favorite-created date behavior without redefining favorite persistence.
- **FR-019**: The product MUST provide non-empty accessible labels or state for image browsing, image positions, placeholders, favorite actions, and description expansion; interactive controls MUST meet the inherited 48 by 48 dp minimum target.
- **FR-020**: The product MUST retain the official fixed-detail limitation, keep `propertyCode` as the local favorite identity, and MUST NOT fabricate a dynamic detail endpoint, address, or structured location from free-form description text.

### Key Entities *(include if feature involves data)*

- **Property Image**: An ordered property-media reference from a supported source, with a valid URL, optional supported semantic tag, and position in the displayed sequence.
- **Property Ad**: The existing core listing entity. This feature uses its supported media and summary information without replacing its identity or favorite contract.
- **Property Details**: The existing core fixed-detail entity. This feature presents supported detail media, facts, description, characteristics, and energy information without implying that it is a dynamic per-listing response.
- **Favorite**: The existing persistent user-owned state keyed by the selected listing's `propertyCode`, including its inherited creation date.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: In acceptance data with multiple valid image URLs, 100% of valid source positions are reachable in their original order on both listing and detail screens, and each multi-image view reports the correct current position and total.
- **SC-002**: In gesture acceptance trials, 100% of horizontal media drags leave the user on the current screen and unchanged favorite state, while 100% of subsequent deliberate card taps open the corresponding detail journey.
- **SC-003**: In all single-image, no-image, and individual-image-failure acceptance cases, the property card or detail remains readable, its media bounds remain stable, and its placeholder is understandable to assistive technology.
- **SC-004**: In a review of supported detail content, a user can identify property type/operation, price, and available essential facts before reaching the description, without reading raw source terminology.
- **SC-005**: For every tested long description, a user can reveal the complete original text with one explicit expansion action and collapse the view without data loss.
- **SC-006**: In accessibility acceptance checks, 100% of image browsing, placeholder, favorite, and description-expansion controls expose a useful label or state, and all interactive targets meet the inherited minimum size.

## Assumptions

- The official challenge listing and fixed-detail responses remain the source of truth; the currently observed response counts and values are examples, not product limits.
- The existing core feature continues to own networking failure/retry states, navigation, persistent favorites, favorite dates, and local `propertyCode` identity.
- The selected listing context is not composed with unrelated fixed-detail content when doing so could mislead the user about which property supplied the information.
- The feature's user-facing copy is Spanish, consistent with the existing core feature.

## Scope Boundaries and Inherited Behavior

This feature supersedes only the core UI specification's single-image and no-carousel guidance. It inherits the core feature's two primary journeys, normal back navigation, persistent favorite and favorite-date behavior, static-detail disclosure, Spanish copy, accessible interaction expectations, existing visual direction, and mandatory primary-screen constraints.

The feature does not add a full-screen gallery, zooming, sharing, downloading, image uploading or editing, agency contact, calls, map browsing, mortgage simulation, payments, reservations, profiles, notes, or saved searches. It does not alter runtime interfaces, navigation destinations, persistence, architecture, dependencies, or the official endpoint contract.
