# Implementation Plan Index

This file is a stable reviewer-facing index. The executable plan is
[specs/001-idealista-core/plan.md](../../specs/001-idealista-core/plan.md); the
ordered implementation work is [specs/001-idealista-core/tasks.md](../../specs/001-idealista-core/tasks.md).

## Delivery sequence

1. Stabilize the Android toolchain and add the smallest shared app foundation.
2. Deliver listing browsing and its loading, empty, error, and retry states.
3. Deliver detail navigation using the fixed challenge response and local selected ID.
4. Deliver Room-backed favorites and favorite-date synchronization on both screens.
5. Run unit, integration, and core XML UI tests, then complete accessibility and
   reviewer documentation checks.

No item in this index supersedes the acceptance criteria or task dependencies in the
canonical feature artifacts.
