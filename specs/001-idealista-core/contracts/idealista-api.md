# Idealista API Contract

## Listing

- Method: `GET`
- URL: `https://idealista.github.io/android-challenge/list.json`
- Response: JSON array of property-ad objects.
- Required identity: `propertyCode` on each returned item.
- The client must accept zero, one, or many items and ignore unknown fields.

### Observed wire format

The following structure was observed on 2026-08-27 and will be captured for deterministic
tests in the dated fixture described in `research.md`:

| JSON path | Wire type | Mapping rule |
|---|---|---|
| `propertyCode` | string | Required, non-blank local identity. |
| `thumbnail` | string or absent | Optional image URL. |
| `price` | JSON number or absent | Fallback amount only when nested amount is absent. |
| `priceInfo.price.amount` | JSON number or absent | Preferred display amount. |
| `priceInfo.price.currencySuffix` | string or absent | Use only with the preferred nested amount. |
| `size` | JSON number or absent | DTO preserves decimal input; map to `Int?` only when non-negative, mathematically integral, and in range. Otherwise omit it. |
| `rooms`, `bathrooms` | JSON integer or absent | Optional characteristics. |

`priceInfo.price` is a nested object, not a decimal scalar. DTOs must mirror that
shape. The repository selects the nested amount and suffix as one pair; if the nested
amount is missing it uses top-level `price` and exposes no nested suffix. A response
with neither amount is a recoverable repository failure.

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
