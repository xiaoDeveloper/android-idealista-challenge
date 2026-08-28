# UI Specification: Property Browsing Experience

**Feature**: `002-property-browsing-experience`

**Status**: Approved implementation guidance

**Scope**: UI behavior added to the inherited listing and fixed-detail journeys

## Purpose and boundaries

This document specifies only the UI delta introduced by this feature. The core
feature continues to own screen structure, navigation, loading/retry/empty states,
networking, persistent favorites, favorite identity and creation date, Spanish copy,
theme tokens, and the fixed-detail disclosure.

The existing XML Fragment and ViewBinding approach remains in place. Media paging is
implemented with the existing RecyclerView dependency and page snapping; this feature
does not add ViewPager2, a gallery destination, zoom, or a new navigation action.

## Shared paged-media behavior

- A media sequence contains every non-blank `multimedia.images[].url` in response
  order. A failed load remains in the sequence and keeps its original position.
- Listing thumbnails are a single-image fallback only when the multimedia sequence is
  empty. A thumbnail is never appended to or duplicated within multimedia.
- The media viewport keeps fixed bounds while pages load, fail, or are absent. Images
  use `centerCrop`; placeholders use the inherited neutral surface treatment.
- Horizontal movement snaps to one image at a time. There are no previous/next arrow
  buttons and no automatic advancement.
- A compact position pill is overlaid at the bottom end of the media viewport for
  multi-image sequences. It uses localized text in the form `1 / 7`, updates after a
  page settles, and is hidden for zero or one image.
- The visible position pill is not a separate accessibility stop. The current page
  communicates its label and position to assistive technology.
- A successfully loaded page with a supported semantic tag is announced in the form
  `Salón, foto 1 de 7`. Without a supported tag it uses `Foto 1 de 7`. A failed page
  uses `Imagen 1 de 7 no disponible`; the sole no-image placeholder uses `Imagen de la
  vivienda no disponible`.
- Only these source tags are translated: `livingRoom` → `Salón`, `bedroom` →
  `Dormitorio`, `kitchen` → `Cocina`, `bathroom` → `Baño`, `facade` → `Fachada`, and
  `corridor` → `Pasillo`. Missing and all other tags are omitted rather than announced
  as raw values.

## Listing-card changes

### Media states

| Available media | Card presentation |
|---|---|
| Two or more valid multimedia URLs | Horizontal pager in the inherited 16:9 media bounds with the current/total pill |
| One valid multimedia URL | One image in the same bounds; no pill or paging affordance |
| No multimedia URL, valid thumbnail | Thumbnail as the sole image; no pill or paging affordance |
| No valid URL | One accessible placeholder in the normal media bounds; no pill |
| One URL fails to load | Placeholder only at that position; adjacent images and the truthful total remain available |

Each recycled card restores its settled page for the same `propertyCode` while it
remains in the adapter data set. A new or replaced property begins at the first page.

### Swipe, tap, and favorite separation

- A horizontal drag inside media belongs to the pager. Ending that drag does not open
  detail and does not toggle favorite state.
- A deliberate tap on a media page opens the inherited detail destination for that
  card's `propertyCode`. A deliberate tap elsewhere on the card, except the favorite
  control, does the same.
- The favorite control remains an independent 48 by 48 dp target immediately below
  and adjacent to the media in the summary row. It is not placed over the swipe area.
- Favorite selection, tint, accessible label, persistence, and saved-date behavior are
  unchanged. Paging never resets or derives favorite state.

## Detail-screen changes

### Media presentation

The first content region is a larger 4:3 paged media area. It follows the shared media
rules above, except detail has no thumbnail fallback. Back navigation and the inherited
favorite control remain outside the pager and independently operable. Tapping a detail
image has no additional action.

### Information hierarchy

After media, content is presented in this order:

1. A short property-type and operation eyebrow, using only supported Spanish values
   such as `Piso · Venta` or `Piso · Alquiler`.
2. Price, using the inherited localized number formatting and supported currency.
3. A human-readable location only when a truthfully associated source supplies it.
   The current fixed response's coordinates are not shown as location text.
4. Essential facts: constructed area, rooms, bathrooms, floor, `Interior` or
   `Exterior`, and `Con ascensor`, when each value is supported and meaningful.
5. The inherited favorite action and saved date for the selected `propertyCode`.
6. Additional supported characteristics, followed by description, followed by energy
   certification.
7. The inherited fixed-detail notice.

Unknown source values, raw keys, coordinates, and guessed address data are never used
to fill an empty row. Empty headings and groups are removed.

### Characteristics

- Facts use short Spanish labels and predictable ordering. Numeric values are shown
  only with verified units.
- `exterior=false` is presented as `Interior`; `exterior=true` is `Exterior`.
- `lift=true`, `boxroom=true`, and `isDuplex=true` may be shown as `Con ascensor`,
  `Trastero`, and `Dúplex`. Their false values are omitted.
- Community costs may be shown as a currency amount, without inventing a monthly or
  annual frequency.
- `unknown`, unsupported fields, technical timestamps, bank flags, and ambiguous
  status values are omitted.

### Expandable description

- A description that fits within six rendered lines is shown in full and has no
  expansion control.
- A longer description initially shows a six-line preview with end ellipsis and a
  48 dp minimum `Ver más` text-button target.
- Activating `Ver más` reveals the complete original text, including source paragraph
  breaks, and changes the action to `Ver menos`. Collapsing restores the preview.
- The control exposes its action and expanded/collapsed state to accessibility
  services. Expanding does not move the user to another screen or alter source text.
- If no description is available, the inherited unavailable-description treatment is
  retained and no expansion control is shown.

### Energy information

- A dedicated `Certificación energética` section appears only when at least one valid
  classification is available.
- Consumption and emissions are separate text rows, for example `Consumo: E` and
  `Emisiones: E`. Valid classifications are the supported letter grades A through G,
  normalized to uppercase.
- The letter is always present in text. Color may support the hierarchy later but is
  never the only carrier of meaning. Invalid or missing classifications are omitted;
  an empty section is not rendered.

## Accessibility and adaptive behavior

- Every media page, failed-image placeholder, no-image placeholder, favorite action,
  and description expansion action has non-empty localized accessibility text.
- The horizontal pager exposes standard forward/backward scroll actions and announces
  the newly settled page without creating a focus stop for decorative indicator text.
- Focus order follows the visual hierarchy: media, property identity, price, location
  when present, facts, favorite/date, additional characteristics, description action,
  energy information, and fixed-detail notice. Back navigation remains first at screen
  level.
- All interactive targets retain the inherited 48 by 48 dp minimum. Pager media itself
  is a click target only on listing cards, where it opens detail.
- Existing theme tokens and contrast requirements apply to indicator surfaces, text,
  placeholders, controls, and focus states. Meaning is never communicated by color
  alone.
- At 200% font scale, indicators remain inside media bounds, facts and headings wrap,
  the description action remains reachable, and no fixed-height text container clips
  content.
- Switch access, keyboard navigation, and TalkBack can reach favorite and expansion
  controls without requiring a swipe gesture. Reduced-motion behavior is naturally
  respected because paging has no autoplay or decorative animation.

## Verification scenarios

1. Bind listing media sequences of zero, one, and several images; verify fallback,
   indicator visibility, source order, and stable card bounds.
2. Force one middle URL to fail; verify only that page shows a placeholder and the
   indicator total does not shrink.
3. Swipe listing media, then activate favorite and deliberately tap the card; verify
   each action remains independent and uses the same `propertyCode`.
4. Bind detail images with supported, unsupported, and missing tags; verify visible and
   assistive text never exposes raw tags.
5. Verify the detail reading order, omission rules, and absence of coordinate-based
   location text against the dated observed detail fixture.
6. Expand and collapse a multi-paragraph description; verify exact full text recovery
   and accessibility state.
7. Verify consumption and emissions grades remain understandable with color disabled,
   at 200% font scale, and with TalkBack enabled.
