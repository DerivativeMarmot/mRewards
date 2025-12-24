# Feature Implementations

## Card (step-by-step, pending unless checked)
- [ ] Creation flow: Select issuer then card product; app loads the correct template based on selection (single template for now). Add minimal form for open date, statement cut, status, welcome-offer progress.
- [ ] Card face display: Show built-in card face on list/detail (placeholder asset now; replace later).
- [ ] Editable fields: Allow editing all fields except issuer (fixed after creation); no validation initially.
- [ ] Delete behavior: Delete card and all attached data (benefits, applications, usage, notifications) with an undo snackbar.
- [ ] Filters: Combined filters for issuer, benefit type, and annual fee on the card list.
- [ ] Search: Add search bar on the card list.
- [ ] Welcome offer UI: Show spending progress bar and deadline (reminder integration deferred).
- [ ] Card management: Update card details; confirm destructive actions and refresh state.

## Benefit
- Benefit management: Add/update/remove benefits on a card; confirm destructive actions and refresh state.
- Benefit types: Support types such as Credit and Multiplier (extendable). Each benefit defines amount, cap, refresh frequency (once/monthly/quarterly/annual), category/merchant, enrollment requirement, effective/expiry dates, and notes/terms.
- Usage tracking UI: Add per-benefit usage entries (date, amount, note), update remaining value, and show history with `MM/dd/yyyy hh:mm` UTC validation/local display.

## Notification
- Notification scheduling: Add triggers (days-before-expiry, statement cut offset, spend remaining thresholds); schedule/dispatch with UTC storage and local display.

## App Settings / Platform
- Config refresh (remote/local): Manual refresh from URL/local JSON with schema validation, hash/signature check, and “last known good” rollback; update template catalog only.
- Currency/localization: Support currency codes; format amounts/dates per locale while storing UTC/raw values.
- Auth and sync: Optional sign-in and cloud backup/sync; keep sensitive card info encrypted locally.
- Data import/export: Export user data to JSON; import with validation and duplicate handling.
- Accessibility/testing: Add semantics tags for Compose UI tests; improve empty/error/loading messaging and color contrast.

## UI
- Card faces: Display built-in card face images on list/detail; start with placeholder/fake assets, replace with official later.
- Visual polish: (Discuss later) layout refinements, typography, and component styles once assets are chosen.
