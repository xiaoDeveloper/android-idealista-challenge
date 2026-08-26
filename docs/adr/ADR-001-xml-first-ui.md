# ADR-001: Keep XML as the Primary UI

- Status: Accepted
- Date: 2026-08-26

## Context

The challenge explicitly requires XML Views for the primary screens. The repository
starts with no UI implementation, and adding Compose would not satisfy that requirement.

## Decision

Listing and detail remain XML layouts using ViewBinding. Compose is not part of this
feature and may only be considered later as isolated optional work.

## Consequences

The implementation demonstrates the required Android View toolkit and keeps the
review surface small. It does not demonstrate Compose interoperability in this phase.
