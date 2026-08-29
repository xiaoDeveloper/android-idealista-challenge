# Detail Media Enrichment Plan

## Approach

Extend the existing shared `ImageDto` and `PropertyImage` with nullable
`localizedName`, and add `COMMUNAL_AREAS` to `PropertyImageTag`. Map valid image URLs as
trimmed values; the matching fixed detail collection keeps the first occurrence of each
exact case-sensitive URL in response order. `AdRepository.loadDetails` keeps its current
selected-listing snapshot and identity gate: only a matching detail response with at
least one mapped image replaces `PropertyDetails.images`; a mismatch, detail failure, or
empty mapped detail collection preserves snapshot media.

Known semantic tags resolve to existing Spanish resource labels before a trimmed
`localizedName` fallback. The shared `PropertyImagePagerAdapter` supplies that same
resolution for image accessibility text and Detail's existing position pill. Add only
the `Zonas comunes` tag label and a labeled position resource so Detail can render
`Salón · 1 / 10`.

Make each rendered Detail pager image clickable. Its click opens a full-screen gallery
destination with the Detail screen's current enriched `PropertyImage` list held as
transient navigation state and the clicked zero-based source index supplied as the
initial position. The gallery reuses the shared pager and label resolver, so it neither
refetches nor transforms media; its overlay shows the resolved label when available and
the one-based `current / total` counter. Back navigation returns to Detail without
changing its collection or selected pager position. Add only the destination, its XML
layout/fragment, navigation wiring, and the Spanish gallery chrome needed for this
behavior.

## Verification

Cover official-fixture `localizedName` decoding, `communalareas` support, matching-only
complete-media enrichment, stable URL deduplication and first-entry metadata, mismatch/
failure/empty-detail fallback, and labeled/unlabeled gallery positions. Use the existing
deterministic instrumented app data with distinct image URLs so the gallery total remains
meaningful after deduplication. Add focused instrumented coverage that taps a non-first
Detail pager image, verifies the full-screen gallery opens at that source position,
renders its resolved label and counter, pages through the same ten-image collection, and
returns to Detail. Then run focused tests, `testDebugUnitTest`,
`assembleDebug`, `lintDebug`, focused `connectedDebugAndroidTest`, and `git diff --check`.
