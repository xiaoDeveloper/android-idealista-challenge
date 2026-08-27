# Architecture

## Implemented architecture

The app is a single Kotlin Android module. `MainActivity` hosts a Navigation
Component graph with `ListingFragment` and `DetailFragment`; both screens use XML
layouts and ViewBinding. Each Fragment collects immutable ViewModel `StateFlow` with
`repeatOnLifecycle`.

```text
XML Fragment -> ViewModel -> Repository -> Retrofit/OkHttp or Room
                       \-> immutable UI state
```

`AdRepository` maps the listing and fixed-detail DTOs into immutable app models. The
detail route carries the selected listing `propertyCode` as `selectedAdId`; the fixed
response's `adid` is not used as favorite identity. Room stores one favorite record per
selected ID, and repository `Flow` observations drive synchronized favorite state in
listing and detail screens.

`AppContainer` creates Retrofit, Room, repositories, and ViewModel factories using
manual constructor injection. The implementation has no Hilt graph, domain/use-case
layer, Compose UI, multi-module split, or offline listing cache.

## Source layout

```text
app/src/main/java/com/xiao/idealistachallenge/
├── App.kt, MainActivity.kt
├── core/             # App container, ViewModel factory, shared UI/error helpers
├── data/local/       # Room database, DAO, entities
├── data/remote/      # Retrofit service, DTOs, serializers
├── data/repository/  # Remote and favorite repository implementations
├── model/            # Immutable app models
├── ui/listing/       # Listing Fragment, ViewModel, adapter
└── ui/detail/        # Detail Fragment and ViewModel

app/src/main/res/
├── layout/           # Activity, listing, detail, and listing-card XML
└── navigation/       # Listing-to-detail graph
```

The feature [plan](../../specs/001-idealista-core/plan.md) is authoritative for exact
interfaces and the [ADRs](../adr/) explain durable choices.
