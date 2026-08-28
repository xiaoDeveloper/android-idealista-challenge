# Research Delta: Property Browsing Experience

## Evidence boundary

These facts come from the public-response copies already stored as
`app/src/test/resources/fixtures/idealista/list-observed-2026-08-27.json` and
`app/src/test/resources/fixtures/idealista/detail-observed-2026-08-27.json`. Their
official URL provenance and fixed-detail limitation remain documented in
[../001-idealista-core/research.md](../001-idealista-core/research.md). This feature did
not refresh the live endpoints, so counts and values below are dated observations, not
product limits.

## Image metadata

**Verified facts**:

- Every observed list item has a `multimedia.images` array whose entries contain `url`
  and `tag`. The dated fixture has seven entries per item and uses the tags
  `livingRoom`, `bedroom`, `kitchen`, `bathroom`, `facade`, and `corridor` across the
  response. Every item also has a thumbnail, but the fixture does not establish that a
  thumbnail or any particular image count is always present.
- The fixed-detail fixture has ten image entries. Each contains `url`, `tag`,
  `localizedName`, and `multimediaId`; the observed tags include the six supported tags
  above plus `communalareas`.

**Decision**: Consume only non-blank `url` and the allowlisted semantic `tag`, preserving
array order. Do not depend on `localizedName` or `multimediaId`; unsupported and missing
tags become no semantic tag. Keep thumbnail as a listing-only fallback when the mapped
multimedia sequence is empty.

**Rationale**: URL and source order are required for paging. A closed tag mapping
prevents raw or newly introduced server values from leaking into visible or assistive
text, while the thumbnail rule matches the approved feature behavior.

**Alternatives rejected**: Trust every `localizedName`, expose arbitrary raw tags, or
append the thumbnail to a non-empty sequence. Each would weaken deterministic Spanish
copy or create misleading/duplicate pages.

## Detail characteristics

**Verified facts**:

- The fixed-detail fixture reports `operation=sale`, `propertyType=homes`,
  `extendedPropertyType=flat`, and `homeType=flat`.
- `moreCharacteristics` is heterogeneous. The dated values include numeric
  `communityCosts=330`, `roomNumber=3`, `bathNumber=2`, and `constructedArea=133`;
  Boolean `exterior=false`, `lift=true`, `boxroom=false`, and `isDuplex=false`; string
  `floor="2"`; and low-value or ambiguous values such as
  `housingFurnitures=unknown`, `agencyIsABank=false`, `flatLocation=internal`,
  `status=renew`, and a technical `modificationDate`.
- The fixed response supplies coordinates under `ubication`, but no verified
  human-readable address associated with the selected listing.

**Decision**: Select the most specific non-blank property type in the order
`homeType`, `extendedPropertyType`, then `propertyType`, and retain `operation` for an
allowlisted Spanish presentation mapper. Map supported characteristics to typed
optional fields. Present `exterior=false` as `Interior`; show lift, storage room, and
duplex only when true; retain community cost as an amount without periodicity; omit
unknown, ambiguous, technical, and unsupported values. Keep coordinates out of the
user-facing location.

**Rationale**: Typed optional fields make omission rules explicit and prevent the
current generic string map from exposing raw source keys or inventing semantics.

**Alternatives rejected**: Render the complete map, infer a monthly community-cost
period, parse a location from description text, or combine list location with the
unrelated fixed-detail object.

## Energy certification

**Verified facts**:

- The fixed-detail fixture has a dedicated `energyCertification` object with
  `title="Certificado energético"`, `energyConsumption.type="e"`, and
  `emissions.type="e"`.
- `moreCharacteristics.energyCertificationType` also contains `e`, but it does not
  distinguish consumption from emissions.

**Decision**: Use the dedicated consumption and emissions fields. Normalize only
single-letter A–G values to uppercase typed grades, omit invalid/missing values, and
render separate textual rows. Do not use the generic characteristic as a substitute
for either dedicated value.

**Rationale**: The dedicated object preserves the two meanings and supports an
accessible presentation that does not depend on color.

**Alternatives rejected**: Display the generic `energyCertificationType` once, accept
arbitrary strings as grades, or encode meaning only in a colored badge.
