# Idealista Android Challenge

This repository contains the specification foundation for a small Idealista property
ads application. The intended product lets a user browse listings, open a detail view,
favorite an ad, and see when it was favorited.

## Current status

The `codex/spec-kit-foundation` branch contains the AI-assisted development harness,
product specification, architecture decisions, and implementation plan. The Android
screens and data layer are intentionally not implemented in this phase, so this
README does not claim that the app currently launches or that the Gradle build passes.

## Documentation map

- [Product specification index](docs/product/SPEC.md) → canonical
  [feature specification](specs/001-idealista-core/spec.md)
- [Implementation plan index](docs/plans/IMPLEMENTATION_PLAN.md) → canonical
  [plan](specs/001-idealista-core/plan.md) and [tasks](specs/001-idealista-core/tasks.md)
- [Target architecture](docs/architecture/ARCHITECTURE.md)
- [Architecture decisions](docs/adr/)
- [AI-assisted workflow](docs/ai/AI_USAGE.md)
- [Project constitution](.specify/memory/constitution.md)

## Challenge API

The official challenge supplies a listing response and a fixed detail response:

- <https://idealista.github.io/android-challenge/list.json>
- <https://idealista.github.io/android-challenge/detail.json>

The detail endpoint always returns the same object. The selected listing's
`propertyCode` is therefore retained as local navigation context for favorite state;
the limitation and its user-visible consequences are documented in the feature spec.

## Planned technical direction

The approved plan targets a single-module Kotlin app with XML layouts, one Activity,
two Fragments, Navigation Component, ViewModels with StateFlow, Retrofit/OkHttp,
Kotlin serialization, Coil, Room, Coroutines, and manual constructor injection. The
first implementation task stabilizes the baseline at AGP 9.3.1, Gradle 9.5.0,
compile/target SDK 36, min SDK 24, and Java/Kotlin 17.

## AI-assisted development

See [AI_USAGE.md](docs/ai/AI_USAGE.md) for the tools, context files, human review
points, and verification evidence used for this repository. The committed `.agents`
and `.specify` directories are part of the reproducible AI workflow.
