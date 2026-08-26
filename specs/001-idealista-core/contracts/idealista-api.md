# Idealista API Contract

## Listing

- Method: `GET`
- URL: `https://idealista.github.io/android-challenge/list.json`
- Response: JSON collection of property ads.
- Required identity: `propertyCode` on each returned item.
- The client must accept zero, one, or many items and ignore unknown fields.

## Detail

- Method: `GET`
- URL: `https://idealista.github.io/android-challenge/detail.json`
- Response: one JSON detail object.
- Current observed remote identity: `adid=1`.
- The response is intentionally static and has no supported property-ID parameter.

## Local route contract

The listing-to-detail route carries `selectedAdId`, sourced from the selected item's
`propertyCode`. It is local UI context for favorite state and is not appended to the
remote detail URL. Remote detail content and local favorite identity must remain separate.

## Failure contract

Transport errors, non-success responses, malformed JSON, and missing required identity
must become a recoverable repository error. UI layers receive a stable user-facing
message and retry action; raw exception messages are not part of the UI contract.
