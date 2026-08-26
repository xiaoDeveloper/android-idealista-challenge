# AI-Assisted Development Workflow

AI use is an explicit challenge requirement. This file records the reproducible
workflow and human review boundaries rather than storing private chat transcripts.

## Tools and roles

- Codex provided repository exploration, API payload inspection, architecture
  alternatives, documentation drafts, and verification orchestration.
- GitHub Spec Kit v1.0.1 provides the project constitution, feature-spec, planning,
  checklist, task, and cross-artifact analysis skills under `.agents/` and `.specify/`.
- Human decisions locked the scope, English repository documentation, Spanish UI,
  XML-first navigation, manual injection, Room persistence, and the fixed-detail
  `propertyCode` favorite semantics.

## Review boundaries

AI suggestions are not accepted without checking Android lifecycle behavior,
coroutine/threading boundaries, JSON field assumptions, accessibility, dependency
cost, and testability. Product behavior belongs in the canonical feature spec;
technical choices belong in the plan or an ADR.

## Evidence

- Official challenge repository and JSON payloads were inspected before defining the
  data contract.
- Spec Kit was pinned to the official v1.0.1 release and initialized with the Codex
  integration and PowerShell scripts.
- `specify integration status --json` is used to verify the generated harness.
- Future implementation commits must include fresh Gradle/test output; this foundation
  branch does not claim an implemented or passing Android build.
