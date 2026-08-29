# Idealista Android Challenge

This repository implements a small Idealista property-ads application. Users can
browse listings, open a fixed-detail view, favorite or unfavorite an ad, and see the
date the current favorite was created.

## Current status

The repository implements listing and fixed-detail journeys, Room-backed favorites
with displayed creation dates, Spanish UI resources, and focused unit and
instrumentation test sources. Run the reproducible procedure in
[quickstart.md](specs/001-idealista-core/quickstart.md) for current validation results.

## Documentation map

- [Product specification index](docs/product/SPEC.md) -> canonical
  [feature specification](specs/001-idealista-core/spec.md)
- [Implementation plan index](docs/plans/IMPLEMENTATION_PLAN.md) -> canonical
  [plan](specs/001-idealista-core/plan.md) and [tasks](specs/001-idealista-core/tasks.md)
- [Implemented architecture](docs/architecture/ARCHITECTURE.md)
- [Architecture decisions](docs/adr/)
- [AI-assisted workflow](docs/ai/AI_USAGE.md)
- [Project constitution](.specify/memory/constitution.md)

## Challenge API

The official challenge supplies a listing response and a fixed detail response:

- <https://idealista.github.io/android-challenge/list.json>
- <https://idealista.github.io/android-challenge/detail.json>

The detail endpoint always returns the same object. The selected listing therefore
remains the source of truth for core Detail content, and its `propertyCode` is retained
as local navigation and favorite identity. The fixed response is used only as
identity-matched enrichment; the limitation and its user-visible consequences are
documented in the feature spec.

## Implementation

The app is a single Kotlin Android module with XML/ViewBinding screens, one Activity,
two Fragments, Navigation Component, ViewModels with StateFlow, Retrofit/OkHttp,
Kotlin serialization, Coil, Room, Coroutines, and manual constructor injection. It
uses AGP 9.3.1, Gradle 9.5.0, compile/target SDK 36, min SDK 24, and Java/Kotlin 17.

## AI-assisted development

See [AI_USAGE.md](docs/ai/AI_USAGE.md) for the tools, repository decisions, human
review boundaries, and verification procedure. The committed `.agents` and `.specify`
directories are part of the reproducible AI workflow.
