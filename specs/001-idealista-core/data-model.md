# Data Model: Idealista Core

## PropertyAd

Represents one listing from the list response.

| Field | Type | Rule |
|---|---|---|
| `propertyCode` | String | Required stable local identity; non-blank |
| `thumbnailUrl` | String? | Optional HTTPS image URL |
| `price` | Decimal | Display without losing currency precision |
| `currencySuffix` | String? | Optional currency display suffix |
| `propertyType` | String? | Optional source value |
| `address` | String? | Optional source value |
| `municipality` | String? | Optional source value |
| `district` | String? | Optional source value |
| `sizeSquareMeters` | Int? | Optional non-negative size |
| `rooms` | Int? | Optional non-negative count |
| `bathrooms` | Int? | Optional non-negative count |
| `description` | String? | Optional long description |
| `imageUrls` | List<String> | May be empty |

Unknown optional source fields are ignored rather than copied into UI contracts.

## PropertyDetails

Represents the fixed detail response plus the selected local context.

| Field | Type | Rule |
|---|---|---|
| `selectedAdId` | String | Comes from the list route's `propertyCode` |
| `remoteAdId` | Int | Current response value, observed as `1` |
| `price` | Decimal | From detail response |
| `description` | String? | From detail response |
| `imageUrls` | List<String> | May be empty |
| `latitude` / `longitude` | Decimal? | Optional coordinates |
| `characteristics` | Map<String, String> | Optional displayable attributes |

`selectedAdId` is never replaced with `remoteAdId`; the latter is response metadata.

## Favorite

Represents the current favorite state for one selected listing.

| Field | Type | Rule |
|---|---|---|
| `adId` | String | Primary key; matches `PropertyAd.propertyCode` |
| `favoritedAtEpochMillis` | Long | Required UTC instant; created on favorite action |

State transitions are `absent -> present(now)`, `present(old) -> absent`, and
`absent -> present(newNow)`. UI formatting uses the active device locale/time zone and
never stores a formatted date as the source of truth.
