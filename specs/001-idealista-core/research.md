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
