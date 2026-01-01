# Feature Implementations

## Card (step-by-step, pending unless checked)
- [x] Creation flow: Select issuer then card product; app loads the correct template based on selection (single template for now). Add minimal form for open date, statement cut, status, welcome-offer progress.
- [x] Card index page
- [ ] Card face display: Show built-in card face on list/detail (placeholder asset now; replace later).
- [x] Editable fields: Allow editing all fields except issuer (fixed after creation); no validation initially.
- [x] Delete behavior: Delete card and all attached data (benefits, applications, usage, notifications) with an undo snackbar.
- [ ] Welcome offer UI: Show spending progress bar and deadline (reminder integration deferred).
- [ ] Card management: Update card details; confirm destructive actions and refresh state.

## Benefit (pending unless checked)
- [x] Benefit management: Add/update/remove benefits on a card; confirm destructive actions and refresh state.
- [x] Benefit types/properties: Support two types:
  - Credit: fixed amount; refresh cadence (once/monthly/quarterly/annual); category/merchant; effective/expiry; notes/terms. One-time credits use a checkbox; multi-use credits store each credit amount used.
  - Multiplier: decimal rate (e.g., 0.05 for 5%/5x), optional cap on spend only; refresh cadence; category/merchant; effective/expiry; notes/terms.
- [x] Categories: Store issuer-scoped categories (e.g., “Dining (Chase)”, “Online Shopping (Amex)”) from a selectable list of common types (Dining, Grocery, Online Shopping, Travel, Gas, Drugstore, Streaming, Transit, Utilities) with ability to add custom types; support multi-select per benefit and only show categories for the benefit’s issuer.
- [x] Usage tracking UI: Multiplier tracks spend amount and derived rewards per transaction; credits track checkbox for single-use or per-use amounts for multi-use; history with `MM/dd/yyyy hh:mm` UTC stored, local display.
- [ ] Status display: Show benefit status (active/expired/paused).

## Offers


## SUB (sign-up bonus)
- [x] done

## Notification
- Notification scheduling: Add triggers (days-before-expiry, statement cut offset, spend remaining thresholds); schedule/dispatch with UTC storage and local display.

## App Settings / Platform
- Config refresh (remote/local): Manual refresh from URL/local JSON with schema validation, hash/signature check, and “last known good” rollback; update template catalog only.
- Auth and sync: Optional sign-in and cloud backup/sync; keep sensitive card info encrypted locally.
- Data import/export: Export user data to JSON; import with validation and duplicate handling.
- Accessibility/testing: Add semantics tags for Compose UI tests; improve empty/error/loading messaging and color contrast.

## UI
- Card faces: Display built-in card face images on list/detail; start with placeholder/fake assets, replace with official later.
- Visual polish: (Discuss later) layout refinements, typography, and component styles once assets are chosen.
- [ ] Selection UI: Use modal dialogs for option selection (type, frequency, status, etc.) with radio-button rows that accept taps on the entire row/label. Do not add a close button.
- [ ] After entity creation or editing, clear the input fields or set them to default value.
- 12dp is the smallest acceptable font size.

## Card Configuration
- Split config by issuer (e.g., `chase.json`, `citi.json`), still validated/merged to a single in-app catalog at load time.
- On first launch, download configs and cache locally; allow users to edit local configs.
- Add a “Sync” button to pull remote configs and override local configs only (user-added cards remain untouched).
- Provide schema validation across merged configs and a “last known good” fallback to avoid corrupting the catalog.

## User Card List
Default page when page starts. Displays all user added cards.
- [ ] Filters: Combined filters for issuer, benefit type, and annual fee on the card list.
- [ ] Search: Add search bar on the card list.

## All Card List
Displays all card products. Used as templates
- [ ] Filters: Combined filters for issuer, benefit type, and annual fee on the card list.
- [ ] Search: Add search bar on the card list.
