<!--
Sync Impact Report
- Version: template -> 1.0.0 (initial project constitution)
- Added principles: challenge compliance, simple boundaries, testable behavior,
  reviewer evidence, and safe AI-assisted changes
- Added sections: Engineering Constraints, Delivery Workflow
- Removed: generic example placeholders from the generated scaffold
- Follow-up: feature-specific behavior belongs in specs/001-idealista-core/
-->

# Idealista Challenge Constitution

## Core Principles

### I. Challenge Compliance Is Non-Negotiable
The implementation MUST satisfy the official challenge requirements before optional
polish: Kotlin, XML Views for the primary UI, listing and detail screens, favorites,
and the date each favorite was created. Optional features MUST NOT weaken or obscure
those requirements.

### II. Prefer the Smallest Clear Boundary
Every abstraction MUST have a current use case and a testability or maintenance
benefit. The project MUST remain a single understandable module unless a later
decision records a concrete reason to split it. Frameworks, layers, and dependencies
MUST NOT be added only to demonstrate familiarity.

### III. Make User-Visible Behavior Testable
State transitions, persistence, networking, and error handling MUST have explicit
contracts and focused tests. UI code MUST observe immutable state and MUST NOT own
networking or database operations. Tests MUST describe behavior rather than private
implementation details.

### IV. Treat Evidence as Part of the Deliverable
Specifications, plans, ADRs, code, and verification output MUST agree. A completion
claim MUST be backed by a fresh command result or an explicitly documented limitation.
The repository MUST distinguish current behavior from approved future design.

### V. Use AI Transparently and Review Its Output
AI tools MAY accelerate exploration, boilerplate, tests, and documentation, but a
developer MUST review generated output for correctness, lifecycle safety, concurrency,
API assumptions, dependency cost, and readability. The repository MUST document the
AI workflow and the human decisions that were retained.

## Engineering Constraints

- The primary application screens MUST use XML layouts. Compose, if ever added, MUST
  remain isolated optional work and MUST NOT replace those screens.
- Network and persistence failures MUST produce understandable recovery states; raw
  exception text MUST NOT be shown as the user-facing message.
- Secrets, tokens, private keys, generated build output, and machine-local settings
  MUST NOT be committed.
- The official Idealista API response is external input. Models and assumptions MUST
  be grounded in observed payloads and recorded in the feature research artifact.

## Delivery Workflow

1. Read this constitution, the active feature spec, its plan/tasks, and relevant ADRs
   before changing code or documentation.
2. Use the Spec Kit sequence for feature work: specify, clarify when needed, plan,
   checklist, tasks, analyze, implement, and converge.
3. Keep changes scoped to the active request. Preserve unrelated user work and review
   the complete diff before committing.
4. On Windows, use the repository Gradle wrapper (`.\gradlew.bat`) and report exact
   commands and outcomes. A known environment limitation MUST be called out rather
   than silently bypassed.

## Governance

This constitution is the stable governance layer for the repository. Feature behavior
belongs in `specs/<feature>/spec.md`; technical design belongs in `plan.md`; durable
cross-feature decisions belong in `docs/architecture/` or an accepted ADR; execution
state belongs in `tasks.md`. When documents disagree, the more specific artifact is
updated to restore consistency rather than weakening this constitution.

Amendments require an explicit rationale, a version bump, an updated Sync Impact
Report, and review of affected specs, plans, tasks, and ADRs. Versioning follows
Semantic Versioning: MAJOR for incompatible governance changes, MINOR for new or
materially expanded principles, and PATCH for clarifications. Every implementation
review MUST check constitution compliance before declaring completion.

**Version**: 1.0.0 | **Ratified**: 2026-08-26 | **Last Amended**: 2026-08-26
