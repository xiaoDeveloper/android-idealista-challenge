# Architecture

## Status

This document separates the verified repository baseline from the approved target
design. The current checkout is an empty, single-module Android template with no
Activity, layout, network client, persistence layer, or implemented feature screens.

## Approved target design

The app will remain a single Android application module. `MainActivity` owns the
navigation host; `ListingFragment` and `DetailFragment` use XML layouts and ViewBinding.
Each Fragment collects immutable ViewModel `StateFlow` with lifecycle awareness.

The data flow is:

```text
XML Fragment -> ViewModel -> Repository -> Retrofit/OkHttp or Room
                       \-> immutable UI state
```

The remote repository maps the list and fixed detail DTOs into app models. The detail
route receives the selected listing `propertyCode`; the fixed endpoint's `adid` is not
used as the favorite identity. A Room repository exposes favorite records as Flow,
so list and detail remain synchronized after toggles and process recreation.

Dependencies are assembled with a small Application container and ViewModel factories.
There is no Hilt graph, domain/use-case layer, Compose rewrite, multi-module split, or
offline listing cache in the approved scope.

## Source layout after implementation

```text
app/src/main/java/com/xiao/idealistachallenge/
├── data/local/       # Room database, DAO, entities
├── data/remote/      # Retrofit service, DTOs, serializers
├── data/repository/  # Remote and favorite repository implementations
├── model/            # Immutable app models
├── ui/listing/       # Listing Fragment, ViewModel, adapter, XML
├── ui/detail/        # Detail Fragment, ViewModel, XML
└── core/             # App container and shared UI/error helpers
```

The feature [plan](../../specs/001-idealista-core/plan.md) is authoritative for exact
interfaces and the [ADRs](../adr/) explain the durable choices.
