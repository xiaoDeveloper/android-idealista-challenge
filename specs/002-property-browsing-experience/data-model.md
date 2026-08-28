# Data Model Delta: Property Browsing Experience

This file defines only material extensions to
[../001-idealista-core/data-model.md](../001-idealista-core/data-model.md). `Favorite`,
navigation identity, persistence, repositories, and all unchanged model fields remain
inherited.

## PropertyImage

One valid source position in a property media sequence.

| Field | Type | Rule |
|---|---|---|
| `url` | `String` | Required and non-blank after remote filtering |
| `semanticTag` | `PropertyImageTag?` | Optional closed semantic value; unsupported/missing source tags map to null |

`PropertyImageTag` contains only `LIVING_ROOM`, `BEDROOM`, `KITCHEN`, `BATHROOM`,
`FACADE`, and `CORRIDOR`. Spanish labels belong to resources, not the model.

Sequence rules:

- Preserve valid multimedia entries in source order, including a URL that later fails
  to load.
- Listing `PropertyAd.images` replaces its URL-only sequence. When it is empty, a
  non-blank inherited `thumbnailUrl` is converted at presentation time into one
  untagged image; it is never appended to a non-empty sequence.
- Detail `PropertyDetails.images` has no thumbnail fallback.

## PropertyDetails typed extensions

The inherited `selectedAdId`, `remoteAdId`, `price`, `description`, latitude, and
longitude rules remain unchanged. The URL-only image list and unfiltered
`characteristics: Map<String, String>` are replaced by the following bounded fields:

| Field | Type | Rule |
|---|---|---|
| `images` | `List<PropertyImage>` | Ordered valid detail media; may be empty |
| `currencySuffix` | `String?` | Optional detail price suffix; non-blank when retained |
| `propertyType` | `String?` | Most specific non-blank source value: `homeType`, `extendedPropertyType`, then `propertyType` |
| `operation` | `String?` | Optional source operation; UI displays only supported translations |
| `constructedAreaSquareMeters` | `Int?` | Non-negative exact integral supported value |
| `rooms` | `Int?` | Non-negative supported value |
| `bathrooms` | `Int?` | Non-negative supported value |
| `floor` | `String?` | Non-blank source label; no numeric or ordinal meaning is invented |
| `isExterior` | `Boolean?` | `true` means Exterior; `false` means Interior; null is omitted |
| `hasLift` | `Boolean?` | UI shows only true |
| `hasStorageRoom` | `Boolean?` | UI shows only true |
| `isDuplex` | `Boolean?` | UI shows only true |
| `communityCosts` | `BigDecimal?` | Non-negative amount; no frequency is attached |
| `energyConsumptionRating` | `EnergyRating?` | Dedicated consumption grade A–G only |
| `energyEmissionsRating` | `EnergyRating?` | Dedicated emissions grade A–G only |

`EnergyRating` is a closed enum containing `A`, `B`, `C`, `D`, `E`, `F`, and `G`.
Input matching is case-insensitive; every other value maps to null.

## Presentation state

Media loading success/failure and current page are UI state, not persisted model data.
Listing page position is keyed transiently by inherited `propertyCode`. Description
expanded/collapsed state belongs to immutable `DetailUiState.Content`; the model always
retains the complete original description and its paragraph breaks.

## Relationships and invariants

- `PropertyAd` and `PropertyDetails` each own an ordered list of `PropertyImage`.
- `PropertyDetails.selectedAdId` continues to refer to the selected listing's
  `propertyCode`; `remoteAdId=1` does not replace it.
- No new relationship to `Favorite` is introduced. Favorite timestamps and state
  transitions remain exactly as defined by the core data model.
- No database migration is required because every new field is remote-derived or
  transient presentation state.
