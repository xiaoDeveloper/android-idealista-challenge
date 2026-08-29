# UI Specification: Idealista Core

**Status**: Approved design guidance for future XML View implementation

**Related artifacts**: [spec.md](spec.md), [plan.md](plan.md), [tasks.md](tasks.md),
[data-model.md](data-model.md), and the screenshots under `docs/design/`.

## Purpose and boundaries

This document translates the eight supplied screenshots (two listing and six detail)
into an original, challenge-focused Android UI. The screenshots establish visual
direction and hierarchy; they are not pixel-perfect specifications and do not authorize
copying proprietary assets, logos, branded wording, or unsupported product flows.

The required listing and detail journeys remain XML Views hosted by Fragments, as
defined in [plan.md](plan.md). This document adds no layouts, resources, Kotlin types,
dependencies, navigation destinations, or runtime behavior. It is guidance for the
later resource and layout tasks, especially T004, T015, and T020; those tasks remain
unimplemented.

All user-visible copy is Spanish. Copy must describe only data and actions supported by
the feature contract. In particular, do not add filters, map browsing, reservations,
payments, sharing, discard, notes, profiles, or a photo-gallery interaction.

## Visual direction

The UI is photo-led, calm, spacious, and information-first. It borrows the references'
strong price hierarchy, large media, white or warm-neutral surfaces, simple outlines,
and clear vertical rhythm while establishing the application's own green visual identity.

- Use Android system typography and Material-compatible XML Views; do not reproduce a
  source application's logo, custom icon set, wordmark, or magenta brand treatment.
- Let property media establish visual context, then make price, location, and essential
  facts easy to scan.
- Prefer one clear action per control. Keep decorative treatment restrained so missing
  data and state panels remain understandable.
- Use outlined cards and low or no elevation. Content separation comes primarily from
  surface color, 1 dp strokes, and spacing.

## Theme tokens

`#88B04B` is the fixed brand seed and the light-theme primary color. It is not used
with white text: white on this green does not meet normal-size text contrast. Use the
specified dark foreground for content placed on the primary surface.

### Light theme

| Semantic token | Value | Use |
|---|---:|---|
| `colorBrandPrimary` / `colorPrimary` | `#88B04B` | Primary buttons, selected/favorited treatment, emphasis |
| `colorOnPrimary` | `#17210B` | Text and icons on primary |
| `colorPrimaryContainer` | `#DDF6B5` | Tonal favorite/date and supportive emphasis surfaces |
| `colorOnPrimaryContainer` | `#1E3300` | Content on primary container |
| `colorSecondary` | `#2F5D50` | Links and secondary emphasis |
| `colorOnSecondary` | `#FFFFFF` | Content on secondary |
| `colorBackground` | `#F7F7F2` | Window background |
| `colorSurface` | `#FFFDF7` | Cards, app bars, and state panels |
| `colorSurfaceVariant` | `#E4E8D9` | Placeholder and subdued metadata surfaces |
| `colorOnSurface` | `#1B1C18` | Primary text and icons |
| `colorOnSurfaceVariant` | `#40443A` | Supporting text and icons |
| `colorOutline` | `#707366` | Card and control outlines |
| `colorError` | `#BA1A1A` | Error icon and retry emphasis |
| `colorOnError` | `#FFFFFF` | Content on error-filled controls |

### Dark theme

| Semantic token | Value | Use |
|---|---:|---|
| `colorPrimary` | `#B1D47A` | Accessible dark-theme primary tone |
| `colorOnPrimary` | `#223606` | Content on primary |
| `colorPrimaryContainer` | `#3D5618` | Tonal favorite/date surface |
| `colorOnPrimaryContainer` | `#D3F09C` | Content on primary container |
| `colorSecondary` | `#9BCFC0` | Links and secondary emphasis |
| `colorOnSecondary` | `#00372D` | Content on secondary |
| `colorBackground` | `#11140F` | Window background |
| `colorSurface` | `#171B14` | Cards, app bars, and state panels |
| `colorSurfaceVariant` | `#41483A` | Placeholder and subdued metadata surfaces |
| `colorOnSurface` | `#E3E4DC` | Primary text and icons |
| `colorOnSurfaceVariant` | `#C1C8B8` | Supporting text and icons |
| `colorOutline` | `#8B9281` | Card and control outlines |
| `colorError` | `#FFB4AB` | Error emphasis |
| `colorOnError` | `#690005` | Content on error-filled controls |

Color communicates grouping and emphasis, never favorite state or an error by itself.

## Typography and dimensions

Use the platform sans-serif typeface and allow the user font scale to determine final
height. Text containers must wrap rather than clip or ellipsize essential information.

| Role | Size | Weight | Intended use |
|---|---:|---|---|
| Detail price | 32 sp | Bold | Main detail price |
| Listing price | 24 sp | Bold | Card price |
| Screen and section title | 22 sp | Medium | App-bar title and content sections |
| Card title | 18 sp | Medium | Property type/location summary |
| Body | 16 sp | Regular | Description and facts |
| Supporting / label | 14 sp | Regular or Medium | Metadata, date, and compact labels |

Use a 4 dp spacing grid. Define reusable dimensions for `space_4`, `space_8`,
`space_12`, `space_16`, `space_24`, and `space_32`.

| Dimension | Value | Rule |
|---|---:|---|
| Phone horizontal content inset | 16 dp | Applies to screen content and RecyclerView card edges |
| Section separation | 24 dp | Between major detail sections and state groups |
| Card internal padding | 16 dp | Applies below property media |
| Compact gap | 8 dp | Between related facts, icon/text pairs, and small controls |
| Card corner radius | 12 dp | Card and media corners align |
| Outline width | 1 dp | Use `colorOutline`; avoid heavy shadows |
| Toolbar height | 56 dp | Excludes system insets |
| Icon size | 24 dp | Standard navigation and favorite icons |
| Minimum interactive target | 48 x 48 dp | Includes favorite, back, and retry controls |
| Button height | 48 dp | Primary and outlined retry actions |

For wider layouts, increase the outer content inset to 24 dp while preserving the same
spacing scale and readable line lengths. Phone portrait is the primary reference.

## Screen hierarchy and reusable elements

The following are future XML-oriented building blocks, not new runtime APIs:

- **Property media**: image container with loading/error placeholder treatment and a
  fixed aspect ratio appropriate to its screen.
- **Property facts row**: icon-optional, compact text row for available area, rooms,
  bathrooms, or another supported characteristic.
- **Favorite control**: a separate clickable control with icon, accessible label, and
  explicit selected state.
- **Favorite date label**: conditional supporting text shown only while favorited.
- **State panel**: centered loading, empty, or error content with an icon, title,
  explanatory text, and optional retry action.
- **Section header**: a 22 sp heading with 24 dp separation before a group of detail
  content.

## Listing screen

### Hierarchy

```text
Listing Fragment
├── Top app bar: "Viviendas"
└── State container
    ├── Loading panel
    ├── Empty panel
    ├── Error panel + retry
    └── Content
        ├── Result count, derived from the rendered response
        └── RecyclerView
            └── Property card (one per returned ad)
```

The app bar is intentionally simple: no copied branding, filter, sort, map, or save
search controls. The result count is a short supporting label such as a localized count
of available viviendas and must not be rendered when the list is empty.

### Property card

Each card represents one returned ad and is selectable as a whole except for its
favorite control, which has its own action. Use a surface background, 1 dp outline,
12 dp corners, and 16 dp space between adjacent cards.

1. **Media**: full-width 16:9 image, `centerCrop`, with top corners matching the card.
   Use the thumbnail or first available image. A neutral surface-variant placeholder
   occupies the exact same 16:9 bounds when loading fails or no image is available.
2. **Summary block**: property type and the most useful available location fields form
   the title. It may wrap; it must not hide the location merely to retain a fixed card
   height.
3. **Price**: 24 sp bold price below the title, including the supported currency suffix
   when present.
4. **Facts**: show up to three available useful facts in a predictable order: size,
   rooms, then bathrooms. Omit absent values rather than using fabricated values.
5. **Favorite**: place an independently focusable favorite icon button in the summary
   block's trailing area. The unselected and selected icon treatments must be visibly
   different. Do not make the entire card toggle favorite state.
6. **Favorite date**: when favorited, show a localized supporting label below the facts,
   for example `Guardado el 26/08/2026`. When not favorited, remove both the label and
   the stored-date presentation.

## Detail screen

### Hierarchy

```text
Detail Fragment
├── Top app bar
│   ├── Back navigation
│   ├── "Detalle de vivienda"
│   └── Favorite control
└── Scrollable state/content container
    ├── Loading panel or error panel + retry
    └── Detail content
        ├── 4:3 swipeable property media and position pill
        ├── Summary: property type/operation and price
        ├── Primary facts and secondary facts
        ├── Favorite-created date, when selected
        ├── Characteristics
        ├── Description
        └── Energy certificate, when present
```

The app bar is a neutral surface with no heavy brand fill. It remains visible for
loading and error states so the user can always return to the listing. It provides one
48 dp heart-shaped favorite action for the selected local property ID only while
content is available. Its label, checked state, outline/filled icon, and the visible
saved date make the state understandable without color or icon alone.

### Detail content

- Use a full-width 4:3 swipeable image pager beneath the app bar, `centerCrop`, with a
  neutral placeholder in the same bounds for absent or failed media. Show a high-
  contrast rounded position pill at the bottom end only when there is more than one
  image.
- Follow the image with 16 dp horizontal content insets, property type/operation,
  32 sp price, primary facts (area, rooms, bathrooms), secondary facts (floor,
  interior/exterior, lift), and the conditional favorite-created date.
- Present available facts as non-interactive, wrapping compact chips. Primary and
  secondary facts use distinct emphasis; omit absent fields without leaving gaps.
- Place storage room, duplex, and community costs in one subtle outlined
  `Características` surface. Community costs use a label/value row and do not infer a
  billing frequency. Omit the section when empty.
- Show the description as a readable 16 sp body section when available. Long text must
  wrap, remain vertically scrollable, and retain its expandable preview behavior.
- Render available consumption and emissions separately as compact outlined label/grade
  items. Do not substitute one missing value with the other or invent energy data.

## Loading, empty, error, and image states

| State | Listing behavior | Detail behavior |
|---|---|---|
| Loading | Show an indeterminate indicator and concise Spanish status text below the app bar. | Show the same treatment while retaining the back app-bar action. |
| Content | Render every returned ad as a card and its current favorite projection. | Render the selected listing's truthful core detail and local favorite state; include fixed-detail enrichment only when the identities match. |
| Empty | Show a calm, centered panel explaining that no viviendas are available; no result count is shown. | Not a normal detail state; inability to resolve the selected listing becomes Error. |
| Error | Show a friendly Spanish explanation, never raw exception text, plus a 48 dp retry action. | Show the same recovery treatment when selected-listing content cannot be resolved; optional fixed-detail failure leaves valid content visible. |
| Image unavailable | Keep all text content visible and replace only the media area with a labelled neutral placeholder. | Keep all detail text visible and replace only the hero media area. |

State labels must be concise and action-oriented, for example: `Cargando viviendas`,
`No hay viviendas disponibles`, `No se han podido cargar las viviendas`, and `Reintentar`.
The later string-resource task owns the final wording.

## Favorite behavior and accessibility

Favorite controls reflect the existing feature contract; they do not introduce a new
data model or route behavior.

- Unfavorited controls expose an action equivalent to `Guardar vivienda` and selected
  controls expose `Quitar de guardados`.
- The accessible state must include whether the property is saved and, when applicable,
  the localized creation date. The visible date uses the device locale and time zone;
  the stored instant remains unchanged.
- Controls use at least 48 x 48 dp hit areas, have non-empty content descriptions, and
  are individually reachable by TalkBack, keyboard, and switch access.
- Traversal order follows the visual reading order: app bar, media, summary, facts,
  favorite control/date, then description and characteristics. A screen-reader user
  must not be forced through decorative media before the property identity.
- Body and supporting text meet a 4.5:1 contrast ratio against their surfaces; icons,
  outlines, and focus indicators meet 3:1 where they communicate meaning.
- Support at least 200% font scale without clipping, overlapping controls, or
  inaccessible favorite actions. Avoid fixed heights for content containing text.

## Implementation constraints for later tasks

- Implement the future screens with XML Views, ViewBinding, `RecyclerView`, and the
  Fragment/ViewModel structure already approved in [plan.md](plan.md). Do not replace
  the required screens with Compose.
- Add colors, dimensions, strings, content descriptions, drawables, and layouts only in
  their assigned future tasks. Use platform or original Material-compatible vector
  icons instead of copied artwork.
- Keep the selected listing `propertyCode` as the favorite identity on the detail
  screen. The static detail response is not evidence that every selected listing shares
  one remote favorite identity.
