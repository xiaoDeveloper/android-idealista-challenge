# AI-Assisted Development Workflow

AI use is an explicit challenge requirement. This file records the reproducible
workflow and human review boundaries rather than private chat transcripts.

## Tools and recorded decisions

- Codex was used for repository exploration, API payload research, documentation,
  implementation assistance, and verification orchestration.
- The repository contains GitHub Spec Kit v1.0.1 configuration and the canonical
  constitution, specification, plan, checklist, tasks, and analysis workflow.
- Accepted repository decisions keep the UI Spanish and XML-first, use manual
  injection and Room-backed favorites, and retain selected `propertyCode` as local
  favorite identity because the official detail response is fixed.

## Review boundaries

AI-generated changes require review for lifecycle behavior, coroutine/threading
boundaries, JSON assumptions, accessibility, dependency cost, readability, and
testability. Product behavior remains canonical in the feature specification;
technical choices remain in the plan and ADRs.

## Evidence

- `research.md` and the API contract record the observed public payloads and dated,
  offline fixtures used by mapping tests.
- The source tree implements the approved XML/Fragment, Retrofit, Room, repository,
  and ViewModel boundaries.
- Unit tests and Espresso instrumentation sources cover mapping, state, persistence,
  and the favorite journey.
- `quickstart.md` defines the reproducible Gradle validation commands; their current
  outcome is reported only after those commands are run.
