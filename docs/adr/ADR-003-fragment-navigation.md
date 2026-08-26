# ADR-003: Navigate Between Two XML Fragments

- Status: Accepted
- Date: 2026-08-26

## Context

The product needs a listing screen and a separate detail screen with a normal back
stack. The app has no navigation infrastructure yet.

## Decision

Use one Activity with a Navigation Component host, `ListingFragment`, and
`DetailFragment`. Pass the selected listing `propertyCode` as the detail route argument.

## Consequences

Back navigation and lifecycle ownership stay centralized, while each screen can own a
focused ViewModel and XML layout. Navigation Component becomes a deliberate dependency
instead of manual FragmentTransaction state management.
