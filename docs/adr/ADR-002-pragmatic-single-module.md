# ADR-002: Use a Pragmatic Single-Module Architecture with Manual Injection

- Status: Accepted
- Date: 2026-08-26

## Context

The challenge is a small app with one feature area. The repository has no existing
module boundaries or dependency-injection graph.

## Decision

Keep one app module with UI, ViewModel, repository, remote, and local data boundaries.
Assemble dependencies through an Application container and constructor injection with
small ViewModel factories. Add a domain/use-case layer only if later business logic
becomes substantial enough to justify it.

## Consequences

The structure is easy to review and unit-test without annotation processing or a
runtime service locator. A future product with materially different boundaries can
record a new decision rather than inheriting speculative modularization.
