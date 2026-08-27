# Research: Idealista Property Browsing and Favorites

## API payload facts

The official challenge repository documents list and detail endpoints and states that
the detail response is always the same:

- <https://github.com/idealista/android-challenge>
- <https://idealista.github.io/android-challenge/list.json>
- <https://idealista.github.io/android-challenge/detail.json>

The observed list payload contains `propertyCode`, `thumbnail`, price, property type,
location, rooms, bathrooms, description, multimedia, and features. The observed detail
payload contains constant `adid=1`, price, multimedia, description, location, and more
characteristics. The list currently contains four ads, but that count is fixture data,
not a product constraint.

### Verified listing wire observation (2026-08-27)

A read-only `GET` to the official listing URL on 2026-08-27 returned HTTP 200 with
`application/json; charset=utf-8` and four listing objects. The following are observed
wire facts for that response, not a promise that the external endpoint will never change:

- `priceInfo.price` is an object containing `amount` and `currencySuffix`; it is not a
  decimal value directly.
- `size` is a JSON number expressed with decimal syntax, for example `133.0` and
  `241.0`, even when its mathematical value is integral.
- The second observed object has top-level `price` `2750000.0`, while its nested
  `priceInfo.price` is `{"amount": 1200.0, "currencySuffix": "€/mes"}`. A display
  amount and suffix must therefore be taken from the same nested price object when it
  is present; the top-level amount must not be paired with that suffix.

Corrective implementation will add a byte-for-byte copied, publicly available response
at `app/src/test/resources/fixtures/idealista/list-observed-2026-08-27.json`. Its
filename, this observation date, and the source URL provide provenance while keeping
unit tests offline. It must not be refreshed implicitly by tests or application code.

### Verified detail wire observation (2026-08-27)

A read-only `GET` to the official detail URL on 2026-08-27 returned HTTP 200. Its
fixed response has `adid` `1`, a top-level numeric `price`, and a **detail-specific**
`priceInfo` object with direct `amount` and `currencySuffix` fields (unlike the list
response's nested `priceInfo.price`). It supplies `propertyComment`,
`multimedia.images[].url`, `ubication.latitude` / `longitude`, and a heterogeneous
`moreCharacteristics` object. The deterministic detail mapping test uses the copied
public response at `app/src/test/resources/fixtures/idealista/detail-observed-2026-08-27.json`;
tests and production code do not refresh it.

## Decisions

### Stable Android baseline

- Decision: use AGP 9.3.1, Gradle 9.5.0, compile/target SDK 36, min SDK 24, and Java/Kotlin 17.
- Rationale: AGP 9.3.1 is the current stable API reference; SDK 36 is already available
  in the baseline environment, while SDK 36.1 is not required by this challenge.
- Alternative rejected: retain AGP 9.3.0-rc01 and compile SDK 36.1, because the current
  checkout cannot resolve the installed platform and is harder for reviewers to reproduce.

### Selected ID for favorites

- Decision: use list `propertyCode` as the local favorite ID and retain it through the
  detail route.
- Rationale: every observed list item exposes it; the detail endpoint's constant `adid`
  cannot distinguish which list item the user selected.
- Alternative rejected: key all favorites to `adid=1`, which would make selecting any
  other item mutate the first item's state.

### Listing wire normalization

- Decision: Retrofit DTOs mirror the verified nested price and decimal-size wire shape;
  `AdRepository` converts those DTOs into the unchanged `PropertyAd` domain model.
- Decision: use `priceInfo.price.amount` and its sibling `currencySuffix` as a matched
  display pair when the nested amount exists. If it is absent, use top-level `price`
  with no nested suffix. If neither amount exists, the listing is invalid.
- Decision: normalize a present `size` only when it is non-negative, mathematically
  integral, and within `Int` range; otherwise omit that optional characteristic without
  failing the entire listing.
- Rationale: this preserves the observed source semantics, prevents incoherent price
  and currency combinations, and keeps the existing domain/UI contract unchanged.

### Persistence and injection

- Decision: Room plus manual constructor injection.
- Rationale: Room makes the favorite-date requirement durable and testable; manual
  injection avoids a DI graph that adds little value in one module.
- Alternatives rejected: in-memory state loses the approved bonus on restart; Hilt/Koin
  add setup and runtime/annotation complexity without a current boundary need.

## Known limitations

The challenge backend does not expose a per-property detail route. The app therefore
displays the fixed detail response after every selection while keeping favorite identity
local to the selected list item. This is documented behavior, not an inferred backend
capability.
