# Product Specification Index

This file is a reviewer-facing entry point, not a second specification. The canonical
product requirements, user stories, acceptance scenarios, edge cases, and success
criteria live in [specs/001-idealista-core/spec.md](../../specs/001-idealista-core/spec.md).

## Product summary

Build a small Spanish-language Android experience for browsing Idealista property ads,
viewing a detail screen, and managing persistent favorites with a visible favorite
date. The official detail endpoint is static, so the selected `propertyCode` remains
the local identity for favorite state while the returned detail payload is displayed
as provided.

## Requirement boundary

The challenge requires Kotlin, XML Views, listing and detail screens, favorites with
favorite dates, and documented AI use. Room persistence and meaningful automated tests
are approved bonus scope. Compose, Hilt, multi-module architecture, pagination, and
offline listing cache are out of scope for this feature.

For implementation details, use the canonical [plan](../../specs/001-idealista-core/plan.md)
and [task list](../../specs/001-idealista-core/tasks.md).
