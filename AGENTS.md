# Agent Instructions

## Purpose

This repository is the Idealista Android Challenge. The current branch establishes
the project specification and AI-assisted development harness; application code is
implemented only when an active feature task explicitly requests it.

## Source of truth

Read documents in this order before making a change:

1. This file and `.specify/memory/constitution.md` for repository-wide guardrails.
2. `specs/<feature>/spec.md` for user-visible behavior and acceptance criteria.
3. `specs/<feature>/plan.md` for technical design and interfaces.
4. `specs/<feature>/tasks.md` for execution order and progress.
5. `docs/architecture/`, `docs/adr/`, and `README.md` for cross-feature context.

`specs/<feature>/` is canonical. The files under `docs/product/` and `docs/plans/`
are reviewer-facing indexes and must link to, rather than duplicate, canonical text.

## Safe, scoped changes

- Inspect the current branch, status, and relevant files before editing.
- Preserve unrelated user work; do not reset, clean, force-push, or delete broadly.
- Keep changes within the active request. Do not introduce a new layer, module,
  dependency, UI paradigm, or product behavior without an updated plan or ADR.
- Do not commit secrets, tokens, private keys, local configuration, build output,
  or IDE state.
- Treat AI output as a proposal. Review lifecycle, concurrency, API assumptions,
  accessibility, security, readability, and dependency cost before keeping it.

## Challenge invariants

The solution MUST remain Kotlin-based, use XML Views for the required screens, provide
listing and detail journeys, support favorite/unfavorite, and show the date a favorite
was created. Optional work must not replace or obscure these requirements.

## Spec Kit workflow

Use the installed Codex skills with `$speckit-constitution`, `$speckit-specify`,
`$speckit-clarify` when needed, `$speckit-plan`, `$speckit-checklist`,
`$speckit-tasks`, `$speckit-analyze`, `$speckit-implement`, and `$speckit-converge`.
Do not install the optional `agent-context`, `git`, or issue-tracking extensions unless
the user explicitly requests them.

## Verification

Use the Windows wrapper for Android checks:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
```

Run only the checks relevant to the change and report exact commands and outcomes.
Do not claim a build or test passed from static inspection. If the environment blocks
a check, record the failure and its cause in the handoff.

## Documentation routing

- Product behavior or acceptance criteria: update `specs/<feature>/spec.md`.
- Technical approach, data flow, or test design: update `plan.md` and related design
  artifacts.
- Durable cross-feature choice: add or amend one focused ADR.
- Implemented architecture: update `docs/architecture/ARCHITECTURE.md` after code
  exists; keep proposed and verified behavior distinct.
- AI roles, human review, and evidence: update `docs/ai/AI_USAGE.md`.

Review the complete diff and `git diff --check` before committing.
