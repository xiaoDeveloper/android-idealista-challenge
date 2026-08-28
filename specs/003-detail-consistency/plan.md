# Detail Consistency Plan

## Approach

`AdRepository` keeps the last successful mapped listing snapshot. `loadDetails` finds
the selected `propertyCode` there, or reloads `list.json` when the snapshot is absent.
It builds core detail fields from that listing. The parameterless `detail.json` remains
optional enrichment and contributes only detail-only facts after its `adid` matches the
selected property identity.

## Files in scope

- `data/remote/IdealistaDtos.kt`, `model/PropertyAd.kt`, and
  `model/PropertyDetails.kt`: retain list fields needed by truthful Detail rendering.
- `data/repository/AdRepository.kt`: cache/fallback resolution and identity-gated
  enrichment.
- `ui/detail/DetailPresentation.kt`, `DetailFragment.kt`, and `fragment_detail.xml`:
  show a supported textual address while continuing to omit coordinates.
- Focused repository, presentation, layout, model, and ViewModel regression tests.

## Error rules

Failure to resolve the selected listing remains a recoverable Detail error. A fixed
detail mismatch or failure leaves the selected-listing content visible and omits
enrichment.

## Verification

Run focused Detail/repository tests, then `testDebugUnitTest`, `assembleDebug`,
`lintDebug`, and `git diff --check`.
