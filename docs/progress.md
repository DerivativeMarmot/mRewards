## 2025-12-23 – Step 1 (Config schema)
- Read `product_functionality.md`, `implementation_plan.md`, `AGENTS.md`, and the empty `architecture.md`.
- Defined JSON config contract in `docs/config_schema.md` (serial IDs, USD amounts, enums, UTC dates `MM/dd/yyyy hh:mm`).
- Added sample config bundle `docs/sample_card_config.json` with one card and benefits aligned to the schema.
- Manual validation against requirements (no automated tests run at this step).

## 2025-12-23 – Step 2 (Config loader and validation)
- Updated build to include Kotlin serialization plugin and JSON library.
- Added config models/enums (`app/src/main/java/com/example/rewardsrader/config/CardConfigModels.kt`).
- Implemented parser with schema/date/enums/card-ref validation (`CardConfigParser.kt`) and asset loader (`CardConfigLoader.kt`).
- Bundled default config snapshot in assets (`app/src/main/assets/card_config.json`).
- Added parser unit tests for success and failure cases (`CardConfigParserTest.kt`).

## 2025-12-23 – Step 3 (Room persistence)
- Added Room dependencies and kapt configuration.
- Created Room entities (Card, Application, Benefit, UsageEntry, NotificationRule) and relation (`CardWithBenefits`).
- Added DAOs for CRUD and joins, plus `AppDatabase`.
- Implemented `CardRepository` to add/list/remove cards with benefits.
- Added `CardRepositoryTest` (Robolectric, in-memory Room) for add/list/join/remove flow.

## 2025-12-23 – Step 4 (Import card from template)
- Extended data to store statement cut and welcome-offer progress in `CardEntity`; bumped `AppDatabase` to version 2.
- Updated `CardRepository` to attach `Application` details and wire `ApplicationDao`.
- Added `CardTemplateImporter` to map a selected config template into card/benefits/application and persist them.
- Added `CardTemplateImporterTest` (Robolectric) covering import flow; adjusted repository test wiring for new fields/DAO.

## 2025-12-23 – Step 5 (Card list & detail UI)
- Added DI container (`AppContainer`) to wire Room DB and `CardRepository`.
- Added viewmodels: `CardListViewModel` (list with loading/error/empty) and `CardDetailViewModel` (detail + applications/benefits mapping).
- Added Compose screens: `CardListScreen` (list with states) and `CardDetailScreen` (metadata, application timeline, benefits).
- Updated `MainActivity` to navigate between list and detail using the viewmodels.
- Added UI tests (`CardScreensTest`) for rendering list/detail content with provided state.

## 2025-12-23 – Step 5.1 (Card creation flow)
- Added issuer/product selection with modal dialogs; renamed “Select template” to “Select product.”
- Expanded bundled config with multiple issuers/products (Chase, Citi, Bank of America, Amex) for selection; all reuse sample benefits for now.
- Added date pickers for open/statement dates (format `MM/dd/yyyy`) and a status selection dialog (pending/approved/denied).
- Form resets after successful create; creation viewmodel/state updated for issuers/templates and reset handling.

## 2025-12-23 – Card editing (editable fields)
- Extended `CardEntity` with `nickname` and `lastFour`; Room DB bumped to v3 and migrations added (1→2, 2→3).
- Repository/DAO now support get/update; tests updated to cover nickname/lastFour.
- Card detail shows nickname/last 4 when present.
- Added edit flow: viewmodel, screen with modal date/status pickers; product/network removed from edit; nickname/last4 editable.
- Edit screen app bar now has back and save (check) icons; bottom buttons removed.
- Added BackHandler in MainActivity to prevent system back from exiting; routes back within screens (detail/create/edit).
- Card detail top bar switched to icon-only (no title); edit screen shows read-only product name.
- Added Go-back icon to edit bar; saved via icon. Room migrations added to AppContainer.
- Added card delete with undo on the card list: delete icon per card, snackbar with undo, snapshot/restore of related data.

## 2025-12-26 – Benefit creation UI enhancements
- Added benefit creation flow with modal selectors for type/frequency, issuer-scoped categories (common + custom), date pickers, and notes; product shown at top.
- Added benefit creation navigation from detail (plus icon in benefits header) and dedicated AddBenefit screen/viewmodel wiring.
- Scoped custom categories per issuer; allow adding via modal and removing custom categories; chips show scoped categories; merchant field removed.
- Updated dividers and labels (expiration date wording) and modal UI consistency (radio/row taps).

## 2025-12-26 – Benefit UI polish & category fixes
- Swapped selection controls to radio-button dialogs with full-row taps for type and frequency; fixed cadence labeling (“annually”) to render horizontally and match selection-UI rules.
- Cleaned the form: show product, drop issuer/merchant inputs, simplify date labels, and keep dividers edge-to-edge.
- Categories header now uses an edit icon opening a modal to add scoped custom categories and remove custom ones; custom entries persist per issuer even after toggling off.
- Category chips merge common issuer-scoped presets with saved custom entries; additions no longer leak across issuers or disappear when deselected.

## 2025-12-27 – Benefit deletion UI
- Added DAO/repository support to delete benefits by id and exposed delete in `CardDetailViewModel` with card reload after removal.
- Card detail UI now shows a trailing trash icon on each benefit card (no swipe); tapping deletes the benefit via the viewmodel.
- Updated tests and wiring to pass benefit IDs; retained Material dependency for icon usage.

## 2025-12-27 – Benefit creation title & reset
- Added a Title field to the Add Benefit form; title is stored on the benefit and shown in detail.
- Cleared Add Benefit form fields back to defaults after a successful save.

## 2025-12-27 – Card detail tabs & scroll
- Redesigned card detail: top card face with issuer/product, tab row for Card Info, Signup Bonus (placeholder), Benefits, Offers (placeholder).
- Card Info tab now holds metadata and application timeline; Benefits tab reuses existing list; others blank for now.
- Switched layout to a single scrollable list so the header and tabs scroll together.

## 2025-12-27 – Inline edits & date pickers
- Card Info fields are inline, with shared dividers and per-field dialogs; edits now update in-memory state without reloading the whole screen.
- Notes are inline and borderless with an icon/value layout; status modal uses radio buttons with capitalized labels.
- Open/statement dates trigger date pickers; edit toolbar icon removed (inline edits only).

## 2025-12-28 – Benefit edit bottom sheet
- Reworked add-benefit into a ModalBottomSheet for both add and edit; sheet closes reliably via shared helper and shows “Edit Benefit” in edit mode.
- Benefit cards are clickable with an edit icon; launching edit preloads existing benefit data into the sheet.
- Benefit viewmodel/state now support edit mode and call update instead of insert when editing.
- Date fields use Material 3 date pickers with inline label/value rows; type/frequency rows also inline/tappable for dialogs.

## 2025-12-28 – Transactions, progress, and SUB persistence
- Multiplier benefits: added transaction modal (amount + date) with list, edit, delete, show-more, and progress bar vs cap; transactions persist via `transactionsJson`.
- Benefit create screens enforce numeric inputs, rate shows %; credit hides cap; amount/rate/cap trimmed to 2 decimals.
- Benefit items simplified to show amount/rate + title only.
- Added SUB spending/duration fields (months/days) with dropdown unit selector, dollar prefix, numeric keyboard; values saved on card and mirrored in UI tabs.
- Database bumped to v5 with migration for SUB fields; AppContainer wired migrations; benefit entity stores transactions JSON.

## 2026-01-01 – Offer features and card detail restructuring
- Added offers data model with Room entity/DAO, migration to DB v7, and repository methods; wired into AppDatabase/AppContainer.
- Implemented offer create/edit flow with validation (credit/multiplier, min/max, dates, status), multiplier field, recommended spend hint, and form reset; added dedicated Compose screen/viewmodel/state.
- Integrated offers into card detail: tab shows offers with add/edit/delete, FAB switches per tab, pager tabs swipeable; date UTC handling fixed.
- Refactored card detail UI into sticky TabRow with HorizontalPager (min height to keep swipe active), removing nested scroll crashes; split shared components/tabs into separate files for clarity.
## 2026-01-06 Firestore sync button
- Added FirestoreSyncer to pull `issuers` and `cards` collections and upsert into Room (doc ID as key, camel/underscore fields accepted).
- AppContainer wires FirebaseFirestore into the sync helper.
- CardListViewModel exposes sync action with IO dispatch, snackbar on success, and reload of list state.
- CardListScreen top bar now includes a sync icon; MainActivity passes the callback.
- Added coroutines play-services dependency for Task await support.

## 2026-01-06 Card creation from local DB
- Card creation state/viewmodel/screen now load issuer and card options from Room via `CardTemplateSource`, filter products by issuer, and pass string IDs; save uses a database-backed importer.
- CardRepository exposes issuer/card getters and `getCardWithBenefits`, and CardTemplateImporter adds `importFromDatabase` to build profile cards/applications from stored templates (linking benefits when present).
- CardCreateViewModelTest updated for the new flow; CardRepositoryTest and CardTemplateImporterTest were refactored to the current schema and all targeted unit tests now pass (`./gradlew testDebugUnitTest --tests ...CardRepositoryTest --tests ...CardTemplateImporterTest --tests ...CardCreateViewModelTest`).

## 2026-01-06 Schema updates
- Added `PaymentInstrument` (Credit, Debit, Charge) and `CardSegment` (Personal, Business) enums to the Prisma schema and wired them into the `Card` model with defaults and column mapping.
- Mirrored the schema in code: Room enums/converters, `CardEntity` fields, Firestore sync mapping, importer defaults, DB version bump (v9), and migration stub for new columns.
- Targeted unit tests remain passing after the schema changes (`./gradlew testDebugUnitTest --tests ...CardRepositoryTest --tests ...CardTemplateImporterTest --tests ...CardCreateViewModelTest`).
