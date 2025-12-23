# Implementation Plan (Basic Functions)

> Prereqs: Read `docs/product_functionality.md` (and `docs/architecture.md` when present) before coding. Keep steps small; validate after each step.

## 1) Define config schema contract (no code)
- Format: JSON. Define required fields: `schema_version`, `data_version`; cards with serial `card_id`, issuer, product_name, network, annual_fee_usd, last_updated (UTC `MM/dd/yyyy hh:mm`), data_source, notes; benefits with serial `benefit_id`, card_id, type (enum: credit/multiplier/access/offer), amount_usd/cap_usd, cadence (monthly/quarterly/annual/once), category/merchant, enrollment_required, effective_date/expiry_date (UTC `MM/dd/yyyy hh:mm`), terms/notes.
- Write the schema doc and a small sample config in `docs/` for reference.
- **Test**: Validate sample JSON against the schema doc manually; confirm every required field matches `product_functionality` and date/currency constraints.

## 2) Add config loader (local bundle)
- Load bundled JSON config (assets/raw) at startup; parse into in-memory models.
- Validate schema_version/data_version, required fields, date format (`MM/dd/yyyy hh:mm` in UTC), and enums; fail gracefully with a fallback message.
- **Test**: Unit test parsing success and failure (bad version/missing fields/date format) using sample JSON; assert validation errors surface and fallback engages.

## 3) Persist core entities (Room)
- Set up Room database with entities: Card, Application, Benefit, UsageEntry, NotificationRule matching schema fields (store dates as strings in UTC `MM/dd/yyyy hh:mm`).
- Implement DAOs/repositories for listing/adding/removing cards and associated benefits.
- **Test**: Room-backed repository unit tests for add/list/remove and relational joins (card with benefits).

## 4) Import card from template
- Add flow to select a card product from the loaded config and pre-populate card + benefits (templates apply only when adding new cards; existing cards stay unchanged).
- Prompt user for personal fields: open date, statement cut, application status, welcome-offer progress.
- **Test**: UI/logic test to select a template, confirm pre-populated benefits, and persist user-entered fields; verify data saved and reloads correctly.

## 5) Card list & detail (read-only)
- Implement a card list screen showing product name, issuer, and status.
- Implement a card detail screen showing benefits (type, amount/cap, cadence, expiry) and application timeline.
- **Test**: UI test to add a card via the flow, then verify list item renders and detail shows the expected benefits and application fields.

## 6) Benefit usage tracking (minimal)
- Allow marking a benefit usage entry (date + amount/value used + note) stored in UTC `MM/dd/yyyy hh:mm`.
- Update remaining value display for the benefit.
- **Test**: UI/logic test to add a usage entry and assert remaining value decreases accordingly; verify persistence across app restart.

## 7) Basic reminders scaffold
- Add data model for notification rules in Room with an enabled flag (no scheduling logic yet).
- Allow toggling reminders per benefit in the detail screen and store the preference.
- **Test**: Unit/UI test to toggle a reminder and verify persisted state; no actual notification dispatch required yet.

## Follow-up: Reminder triggers and scheduling
- Extend notification rules with trigger params (e.g., days_before_expiry, statement_cut_offset, spend_remaining_threshold) and cadence awareness from benefit data.
- Implement scheduling/dispatch respecting local time display while storing trigger times in UTC.
- **Test**: Unit tests for trigger calculation; UI/integration test to schedule a mock notification and assert it fires at the expected local time window.

## 8) Config refresh hooks (without networking)
- Provide a manual “refresh config” action that re-reads the bundled file (placeholder for remote later).
- Preserve user cards/benefits; only update the in-memory template catalog (templates never overwrite existing cards).
- **Test**: Unit test to reload config and confirm existing user data remains unchanged while template catalog updates.

## 9) Error and empty states
- Add user-facing states for empty card list, config load failure, and missing benefits.
- **Test**: UI test to simulate empty repository and failed config load; verify appropriate messages are shown.
