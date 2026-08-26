# ADR-004: Persist Favorites and Creation Timestamps in Room

- Status: Accepted
- Date: 2026-08-26

## Context

Favorite state and the date it was created are core requirements. Persistence is an
official bonus and prevents the feature from losing its meaning after process death.

## Decision

Store one record per selected listing ID with an epoch-millisecond creation timestamp.
Room is the repository-owned persistence boundary. Unfavoriting deletes the record;
favoriting again writes a new timestamp. UI formats the timestamp at the boundary.

## Consequences

Favorites survive application restarts and can be observed consistently from both
screens. The app does not cache remote listings offline, and database backup behavior
will be reviewed when the Android data policy is implemented.
