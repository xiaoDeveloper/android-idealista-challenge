# Feature Specification: Idealista Property Browsing and Favorites

**Feature Branch**: `001-idealista-core`

**Created**: 2026-08-26

**Status**: Approved for implementation planning

**Input**: Official Idealista Android Challenge requirements and the reviewed project
brief in the repository attachments.

## User Scenarios & Testing

### User Story 1 - Browse property listings (Priority: P1)

As a person looking for a home, I want to browse a list of property ads so that I can
compare the available options.

**Why this priority**: Listing discovery is the entry point and provides value even
before a user opens an individual property.

**Independent Test**: Given a successful listing response, a reviewer can launch the
listing journey and see every returned ad represented by its useful summary fields.

**Acceptance Scenarios**:

1. **Given** the listing request is in progress, **When** the listing screen is shown,
   **Then** a clear loading state is visible and the screen remains usable.
2. **Given** the listing request succeeds with one or more ads, **When** the response
   is displayed, **Then** each ad shows an image or placeholder, price, property type,
   location, and basic characteristics.
3. **Given** the listing request succeeds with no ads, **When** the response is
   displayed, **Then** an understandable empty state is shown.
4. **Given** the listing request fails, **When** the failure is shown, **Then** the
   user sees a friendly error and can retry without restarting the app.

### User Story 2 - Open a property detail view (Priority: P1)

As a person comparing homes, I want to open an ad on a separate screen so that I can
read its detailed information.

**Why this priority**: A separate detail journey is a mandatory challenge requirement
and is the natural next step after listing discovery.

**Independent Test**: From any rendered listing, a reviewer can select an item, reach a
separate detail screen, and return to the list with normal back navigation.

**Acceptance Scenarios**:

1. **Given** a listing item is visible, **When** the user selects it, **Then** a detail
   screen opens and the selected listing ID is retained as local context.
2. **Given** the detail request is in progress, **When** the detail screen is shown,
   **Then** a loading state is visible.
3. **Given** the detail request succeeds, **When** the response is displayed, **Then**
   useful price, property, image, location, description, and characteristic information
   is readable.
4. **Given** the detail request fails, **When** the failure is shown, **Then** the user
   sees a friendly error with a retry action and can return to the list.

### User Story 3 - Manage persistent favorites (Priority: P1)

As a person comparing homes, I want to favorite and unfavorite an ad and see when I
favorited it so that I can keep track of my choices.

**Why this priority**: Favorites and the creation date are explicit mandatory behavior,
and the state must remain consistent across both screens.

**Independent Test**: A reviewer can favorite an ad from the list or detail screen,
observe its date, unfavorite it, and relaunch the app to confirm the saved state rules.

**Acceptance Scenarios**:

1. **Given** an ad is not favorited, **When** the user selects its favorite control,
   **Then** it becomes visibly favorited and a creation date is shown.
2. **Given** an ad is favorited, **When** the user opens its detail screen, **Then** the
   detail screen shows the same favorite state and date.
3. **Given** an ad is favorited, **When** the user unfavorites it, **Then** the favorite
   state and date are removed from both screens.
4. **Given** an ad was unfavorited and is favorited again, **When** the new state is
   displayed, **Then** a new creation timestamp is recorded.
5. **Given** a favorite exists before the app process is recreated, **When** the app is
   opened again, **Then** the favorite and its date are restored.

## Edge Cases

- A listing response may contain an arbitrary number of ads, including zero; the UI
  must not assume the current observed count.
- Missing optional image or characteristic fields use a meaningful placeholder or are
  omitted without crashing.
- The detail endpoint always returns the same response, currently identified by
  `adid=1`. The selected listing's `propertyCode` remains the local favorite identity;
  the app must disclose this limitation in reviewer documentation rather than invent a
  detail URL parameter or silently change the response.
- A malformed response, timeout, offline device, or server error shows a recoverable
  error state and never exposes a raw exception as user-facing copy.
- A favorite timestamp is stored as an instant and formatted as a date using the
  device time zone and active locale. Formatting must not replace the stored value.

## Requirements

### Functional Requirements

- **FR-001**: The product MUST show a listing journey that represents every ad returned
  by the official listing source.
- **FR-002**: Each listing summary MUST expose the most useful available image,
  price, property type, location, and basic characteristics without overcrowding.
- **FR-003**: The product MUST provide a separate detail journey reachable from any
  listing and support normal back navigation.
- **FR-004**: The detail journey MUST request and display the official detail response;
  it MUST not manufacture a dynamic endpoint parameter that the challenge does not provide.
- **FR-005**: The product MUST let a user favorite and unfavorite an ad from the list
  and detail journeys.
- **FR-006**: A favorited ad MUST display the date on which the current favorite was
  created, and unfavoriting MUST remove that date.
- **FR-007**: Favorite state and its creation timestamp MUST survive application
  restart and remain synchronized across journeys.
- **FR-008**: Loading, content, empty, error, and retry states MUST be understandable
  for every asynchronous journey where the state applies.
- **FR-009**: Interactive controls MUST have accessible labels, adequate touch targets,
  readable text, and state communication that does not rely only on color.
- **FR-010**: The repository MUST document AI-assisted development, human review
  decisions, known limitations, and reproducible verification commands.

### Key Entities

- **Property Ad**: A listing supplied by the challenge source, identified locally by its
  `propertyCode`, with summary and detail information.
- **Favorite**: A user-owned state for one local property ID, containing the instant at
  which the current favorite was created.

## Success Criteria

### Measurable Outcomes

- **SC-001**: In a successful run, 100% of ads present in the listing response are
  represented by a selectable listing item.
- **SC-002**: From any selectable listing item, a reviewer reaches a separate detail
  view and returns to the list using the system back action without losing list state.
- **SC-003**: In a controlled test, 100% of favorite and unfavorite actions update the
  visible state and date consistently in both journeys.
- **SC-004**: After process recreation, 100% of favorites created before recreation
  retain their local ID and displayed creation date.
- **SC-005**: Every tested loading, empty, network-error, and retry scenario presents a
  user-understandable state with no raw exception text.
- **SC-006**: All required interactive controls have non-empty accessibility labels and
  touch targets that meet the platform's recommended minimum.

## Assumptions

- The challenge endpoints are reachable over HTTPS during normal use; the app does not
  promise offline listing browsing in this feature.
- `propertyCode` is the stable local identity for list items because the list payload
  exposes it for every observed ad; the constant detail `adid` is not used to merge
  different listing identities.
- The app's visible copy is Spanish, while repository documentation is English for
  reviewer portability.
- The current observed list has four ads, but the implementation treats the response as
  variable and does not encode four as a product limit.
