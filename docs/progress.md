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
- Mirrored the schema in code: Room enums/converters, `CardEntity` fields, Firestore sync mapping, importer defaults; DB currently at v12.
- Renamed `foreignFeeTransactionFee` to `foreignTransactionFee` across schema and code (entity, Firestore sync fallback, importer), with migration 9->10.
- CardTemplateImporter deep-copies benefits when adding a card (new benefit IDs linked only to the user’s profile card), keeping template/remote benefits immutable references.
- Categories now rely solely on the enum values (no extra raw field); benefit save/init mappings normalized to enum names; targeted unit tests pass after clean rebuild.

## 2026-01-08 - Schema review
- Read updated Prisma schema with the new `TemplateCard` model meant for Firestore-synced templates.
- Noted drift from Room/code (e.g., `Card.annualFee` missing, TemplateCard not yet represented in entities/DAOs/migrations).
- Shared feedback on aligning TemplateCard usage, table mapping, and indexes; no code changes made yet.

## 2026-01-08 - Schema rename
- Updated Prisma schema to link ProfileCard directly to `card_id` (renamed from `template_card_id`) per template separation plan.
- Refreshed schema documentation to reflect the new ProfileCard linkage, TemplateCard model, and corrected enum spelling.

## 2026-01-08 - Room TemplateCard & cardId migration
- Added Room `TemplateCardEntity`/DAO and wired it into `AppDatabase` (version 14), repository, and Firestore sync (templates mirror card IDs).
- Renamed ProfileCard foreign key to `cardId` across entities, relations, importer, viewmodels, and tests; added migration 13→14 to rename the column and create the template_cards table.
- Updated Prisma/docs to make `TemplateCard.cardId` non-null with cascade and indexed; UI now reads card metadata via the new relation name.

## 2026-01-08 - TemplateCard benefits refactor
- Replaced card-level benefit joins with template-level joins: removed `card_benefits`, added `template_card_benefits` with Room entity/DAO and migration 14→15 that drops old links and migrates existing rows.
- Added `TemplateCardWithBenefits` relation and `TemplateCardBenefitEntity`; repository exposes `getTemplateCardWithBenefits` and upserts template benefit links.
- Updated importer, DAOs, repository usage, and tests to read/write template benefits; AppDatabase bumped to v15 and migration wired.
- Prisma/schema docs updated to drop `CardBenefit`, add `TemplateCardBenefit`, and align Benefit relations.

## 2026-01-08 - Benefit fields and profile link dates
- Added `title` to benefits, removed `enrollmentRequired`, and moved `startDateUtc`/`endDateUtc` to `ProfileCardBenefit` with a new migration to v16.
- Updated Room entities/DAOs/repository/importer to store benefit dates on the profile link and enforce unique `benefitId` per template card; AppDatabase bumped to 16 and migration wired.
- Benefit create/edit flow now reads/writes title and per-profile dates via the updated repository; Card detail mapping uses profile benefit links for expiry.
- Prisma/schema docs reflect the new benefit fields and profile link date storage.

## 2026-01-08 - Firestore template sync
- Firestore sync now pulls `benefits`, `template_cards` (falling back to cards), and `template_card_benefits` in addition to issuers/cards, mapping to Room entities with enum/category parsing.
- Repository upserts template cards/benefits and links; sync result now reports counts for all synced collections.

## 2026-01-09 - Enum extensions
- Added `Supermarket` and `RetailStore` to benefit categories and `EveryTransaction` to benefit frequencies across schema/docs and Room enums.
- Updated importer/VM mappings, category parsing, and frequency selection UI; Firestore sync handles the new categories.

## 2026-01-11 - Product selection dialog scroll
- Updated `CardCreateScreen` selection dialog to use a bounded, scrollable list so long issuer/product catalogs remain accessible.

## 2026-01-11 - Profile card face reference
- Added `cardFaceId` column/relationship to `ProfileCard` (schema docs, Room entity/relation), migration to v17 with indexes, and wired the migration in AppDatabase/AppContainer.
- Card face DAO now exposes preferred face lookup, and card creation importer copies the default/preferred card face ID onto new profile cards.

## 2026-01-11 - Card face display
- Added Coil Compose dependency and surfaced `cardFaceUrl` in card detail state.
- Card detail header now loads the remote card face URL when available, with a simple fallback text placeholder.

## 2026-01-11 - Firestore card face sync
- Firestore sync now pulls `card_faces`, maps remote URLs/default flags, and upserts them; sync result reports face counts.

## 2026-01-11 - Card template importer fix
- Corrected profile card mapping call to use the updated parameter name, fixing card creation compilation.

## 2026-01-11 - Card face header update
- Removed issuer/product text from the card face header and increased image height for a larger card face display.

## 2026-01-11 - Card face picker
- Added card face menu option on card detail; bottom sheet lists faces for the card and saves selection back to the profile card, updating the displayed face.
  - Picker now caps height at half the screen, shows two faces per row, and displays images uncropped.

## 2026-01-12 - Card creation search plan
- Documented the new search-style card creation flow in `docs/feature_implementation.md`, covering search-first layout, sort/filter bottom sheets, card result list, selection-to-form handoff, and testing approach.
- Updated the plan so tapping a search result creates the card immediately via the importer, defaulting open date to today (saved in UTC, shown local) and status to `pending`, leaving optional fields blank; success navigates to the Card List with a snackbar and failures stay on search with an error snackbar.

## 2026-01-12 - Card creation search UI
- Rebuilt the card creation screen into a search-first list with search box, sort chip, and filter bottom sheet (issuer/network/segment/payment instrument, annual fee slider + no-fee toggle, benefit type/category chips); results show issuer/product/network/segment/instrument and fee.
- Card taps now import immediately with open date defaulted to today (stored in UTC) and status `pending`, leaving optional fields blank; success navigates back to Card List and triggers its snackbar, failures stay on search with an error snackbar.
- CardCreateViewModel now emits events, computes filter metadata from template cards with benefits, and applies query/sort/filter reductions; CardListViewModel exposes a notifier for create success; TemplateCardDao/Repository return template cards with attached benefits for filter data.
- Updated `CardCreateViewModelTest` for the new flow and ran `./gradlew testDebugUnitTest --tests com.example.rewardsrader.ui.cardcreate.CardCreateViewModelTest`.
- Added capped height (85%) to the filter bottom sheet so it leaves space under the top bar.
- Made the filter sheet scrollable to reach all fields on smaller screens.
- Added a slim scroll indicator on the filter sheet to show scroll position.
- Issuer chips in the filter sheet now show leading issuer icons pulled from `res/drawable` (amex/chase/citi/bofa/capital one/discover/barclays/hsbc/us bank).
- Card search results now use a three-row layout: product title on top; middle row with a scaled-down card face thumbnail on the left and annual fee on the right; bottom row with benefit category chips. Card faces use default URLs when available and fall back to text.
- Added card preview bottom sheet triggered by tapping a search result: shows card name, full-size card face, large annual fee, and benefit bullet points (title/amount/cadence). Plus icon on list items now adds the card directly; row tap opens the preview.

## 2026-01-15 - Card creation search polish
- Fixed CardCreateScreen modal structure so filter apply/reset closes correctly and list scrolls back to the top after applying sort or filters; filter sheet stays bounded with the header actions outside the scroll area and a visible scroll indicator.
- Scaled card face thumbnails down in result rows while keeping the three-row layout; plus icon still adds directly while row taps open the preview sheet.
- Preview bottom sheet now caps at 80% screen height with a scrolling body, pinned header (title/add), larger annual fee text, and benefit bullets showing title/amount/cadence; uses card title fallback and keeps the close action outside the scrollable area.
- Tests: `./gradlew testDebugUnitTest --tests com.example.rewardsrader.ui.cardcreate.CardCreateViewModelTest`.
- Refactored `CardCreateScreen` into smaller files (`SortSheet`, `FilterSheet`, `CardResultRow`, `CardPreviewSheet`) to reduce file size and isolate UI components; main screen now only wires state/events and delegates rendering to the new components.

## 2026-01-15 - Card creation search UX tweak
- Keep card search results scrolled to the top whenever the search query changes to avoid mid-list positions while typing.

## 2026-01-15 - Card list FAB
- Updated `app/src/main/java/com/example/rewardsrader/ui/cardlist/CardListScreen.kt` to use a Material 3 floating action button for Add card and removed inline Add card buttons from list/empty states.

## 2026-01-15 - Card list snackbar/FAB behavior
- Updated `app/src/main/java/com/example/rewardsrader/ui/cardlist/CardListScreen.kt` so the snackbar sits at the bottom while the Add card FAB animates upward when a snackbar is visible, with list padding adjusted to avoid overlap.

## 2026-01-15 - Card list item layout
- Updated `app/src/main/java/com/example/rewardsrader/ui/cardlist/CardListViewModel.kt` to include card face URL, last four digits, and open date in list state mapping.
- Updated `app/src/main/java/com/example/rewardsrader/ui/cardlist/CardListScreen.kt` to render the cropped card face strip, overlapping name/last four/delete row, and approval-duration plus status row.
- Updated `app/src/androidTest/java/com/example/rewardsrader/ui/CardScreensTest.kt` to align fixtures and assertions with the list model and detail screen signature.

## 2026-01-15 - Card list refresh on return
- Added an on-resume refresh in `app/src/main/java/com/example/rewardsrader/ui/cardlist/CardListScreen.kt` and wired it in `app/src/main/java/com/example/rewardsrader/MainActivity.kt` so list items reflect detail edits after navigating back.
- Updated `app/src/androidTest/java/com/example/rewardsrader/ui/CardScreensTest.kt` for the new CardListScreen parameter.

## 2026-01-15 - Card list layout no overlap
- Updated `app/src/main/java/com/example/rewardsrader/ui/cardlist/CardListScreen.kt` to stack the card face strip and text rows instead of overlapping them to avoid title wrap misalignment.

## 2026-01-16 - Card list long-press actions
- Added a long-press menu to card list items with Duplicate/Delete actions in `app/src/main/java/com/example/rewardsrader/ui/cardlist/CardListScreen.kt`.
- Implemented card duplication via `app/src/main/java/com/example/rewardsrader/data/local/repository/CardRepository.kt` and wired the UI action through `app/src/main/java/com/example/rewardsrader/ui/cardlist/CardListViewModel.kt` and `app/src/main/java/com/example/rewardsrader/MainActivity.kt`.
- Updated `app/src/androidTest/java/com/example/rewardsrader/ui/CardScreensTest.kt` for the new list screen parameter.

## 2026-01-16 - Card list menu styling
- Updated `app/src/main/java/com/example/rewardsrader/ui/cardlist/CardListScreen.kt` to align the long-press menu to the right and force a white background for the menu.

## 2026-01-16 - Card list menu icons
- Added Duplicate/Delete icons to the long-press menu items in `app/src/main/java/com/example/rewardsrader/ui/cardlist/CardListScreen.kt`.

## 2026-01-17 - Tracker spec update
- Updated the `Tracker` section in `docs/feature_implementation.md` with bottom navigation access, tracker sources, status rules, period generation, and edit flow details.

## 2026-01-18 - Tracker feature
- Added tracker persistence: `TrackerEntity`, `TrackerTransactionEntity`, DAOs, and migration 17->18; Room DB bumped to v18 with converters and repository helpers.
- Implemented tracker UI flows: list screen with Active/Complete/Expired filter chips and edit screen for transactions and offer completion.
- Navigation updated with bottom bar tabs (Cards/Tracker) and tracker routes in `app/src/main/java/com/example/rewardsrader/MainActivity.kt`.
- Schema docs updated to include tracker tables/enums (`docs/schema/schema.prisma`, `docs/schema/schema.md`).
- Tests updated for repository constructor signature (`app/src/test/java/com/example/rewardsrader/data/CardRepositoryTest.kt`, `app/src/test/java/com/example/rewardsrader/template/CardTemplateImporterTest.kt`).

## 2026-01-18 - NavHost padding
- Removed top padding from the NavHost by only applying the bottom bar inset in `app/src/main/java/com/example/rewardsrader/MainActivity.kt`.

## 2026-01-18 - Tracker flash fix
- Avoided repeated tracker loads by skipping loading state on resume and using `LaunchedEffect` for tracker detail in `app/src/main/java/com/example/rewardsrader/MainActivity.kt`.
- Added a `showLoading` flag to `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerViewModel.kt` to keep the list from flashing when refreshing.

## 2026-01-18 - Tracker calendar periods
- Aligned monthly/quarterly/semi-annual/annual tracker windows to calendar boundaries with partial first periods while keeping anniversary/offer dates dynamic in `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerViewModel.kt`.

## 2026-01-18 - Offer tracker notes
- Added tracker notes storage with migration 18->19 and DB v19 (`app/src/main/java/com/example/rewardsrader/data/local/entity/TrackerEntity.kt`, `app/src/main/java/com/example/rewardsrader/data/local/Migrations.kt`, `app/src/main/java/com/example/rewardsrader/data/local/AppDatabase.kt`).
- Offer trackers now hide transactions and show notes + save flow (`app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerEditScreen.kt`, `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerEditViewModel.kt`, `app/src/main/java/com/example/rewardsrader/MainActivity.kt`).

## 2026-01-18 - Offer tracker save navigation
- Saving an offer tracker now returns to the tracker list via callback wiring in `app/src/main/java/com/example/rewardsrader/MainActivity.kt` and `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerEditViewModel.kt`.

## 2026-01-18 - Tracker add transaction modal
- Benefit tracker transactions now use a plus-button modal instead of inline inputs in `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerEditScreen.kt`.

## 2026-01-18 - Tracker list grouping
- Grouped trackers by card in the list and redesigned tracker rows with a two-line layout in `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerScreen.kt`.

## 2026-01-18 - Tracker list spacing
- Reduced spacing between tracker items and added rounded corners to first/last items per card group in `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerScreen.kt`.

## 2026-01-18 - Tracker list background
- Matched tracker item background to the theme background color in `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerScreen.kt`.

## 2026-01-18 - Tracker card faces
- Added partial card faces to tracker card groups and grouped by profile card ID in `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerScreen.kt` and `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerState.kt`.

## 2026-01-18 - Tracker scroll restore
- Added a remembered list state so the tracker list keeps its scroll position after returning from detail in `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerScreen.kt`.

## 2026-01-18 - Tracker single item rounding
- Made single tracker items fully rounded within card groups in `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerScreen.kt`.

## 2026-01-18 - Tracker background refresh
- Added WorkManager background refresh to generate new trackers daily (`app/src/main/java/com/example/rewardsrader/data/worker/TrackerRefreshWorker.kt`, `app/src/main/java/com/example/rewardsrader/data/worker/TrackerWorkScheduler.kt`).
- Extracted tracker generation into a shared helper for UI and worker (`app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerGenerator.kt`).
- Scheduled tracker refresh from `app/src/main/java/com/example/rewardsrader/AppContainer.kt` and added WorkManager dependency.

## 2026-01-18 - Card status options
- Simplified card status selection to Pending/Active/Closed in `app/src/main/java/com/example/rewardsrader/ui/carddetail/components/EditDialogs.kt`.

## 2026-01-18 - Card status save fix
- Fixed status selection to persist the chosen value instead of lowercasing in `app/src/main/java/com/example/rewardsrader/ui/carddetail/components/EditDialogs.kt`.

## 2026-01-18 - Card detail benefit/offer list rounding
- Reduced spacing between benefit/offer list items and applied rounded corners only to first/last (fully rounded for single items) in `app/src/main/java/com/example/rewardsrader/ui/carddetail/CardDetailScreen.kt`.
- Updated benefit/offer cards to accept explicit shapes from the list in `app/src/main/java/com/example/rewardsrader/ui/carddetail/components/BenefitOfferCards.kt`.

## 2026-01-18 - Benefit create transaction removal
- Removed the transaction section and related dialog controls from the benefit create/edit UI in `app/src/main/java/com/example/rewardsrader/ui/benefitcreate/BenefitCreateScreen.kt`.
- Simplified the benefit create screen wiring in `app/src/main/java/com/example/rewardsrader/MainActivity.kt` by dropping transaction callbacks.

## 2026-01-18 - Benefit transaction storage removal
- Removed benefit transaction state and helpers from `app/src/main/java/com/example/rewardsrader/ui/benefitcreate/BenefitCreateState.kt` and `app/src/main/java/com/example/rewardsrader/ui/benefitcreate/BenefitCreateViewModel.kt`.
- Dropped legacy transaction storage from the repository and database wiring in `app/src/main/java/com/example/rewardsrader/data/local/repository/CardRepository.kt`, `app/src/main/java/com/example/rewardsrader/data/local/AppDatabase.kt`, `app/src/main/java/com/example/rewardsrader/AppContainer.kt`, and `app/src/main/java/com/example/rewardsrader/data/worker/TrackerRefreshWorker.kt`.
- Added migration 19->20 to remove the legacy `transactions` table and bumped the database to v20 in `app/src/main/java/com/example/rewardsrader/data/local/Migrations.kt` and `app/src/main/java/com/example/rewardsrader/data/local/AppDatabase.kt`.
- Updated schema docs to remove the benefit `Transaction` model in `docs/schema/schema.prisma` and `docs/schema/schema.md`.

## 2026-01-18 - Card detail per-tab scrolling
- Reworked card detail layout so the header and tabs are fixed while each tab content scrolls independently in `app/src/main/java/com/example/rewardsrader/ui/carddetail/CardDetailScreen.kt`.

## 2026-01-18 - Card detail tab padding
- Increased benefits and offers tab content padding in `app/src/main/java/com/example/rewardsrader/ui/carddetail/CardDetailScreen.kt`.

## 2026-01-18 - Benefit frequency option
- Added the missing `semiannually` option to the benefit frequency selector in `app/src/main/java/com/example/rewardsrader/ui/benefitcreate/BenefitCreateScreen.kt`.

## 2026-01-18 - Benefit frequency labels
- Made benefit frequency display values human readable in `app/src/main/java/com/example/rewardsrader/ui/benefitcreate/BenefitCreateScreen.kt`.

## 2026-01-18 - Benefit category merge
- Removed the `Restaurant` benefit category enum and migrated any stored values to `Dining` in `app/src/main/java/com/example/rewardsrader/data/local/entity/Enums.kt` and `app/src/main/java/com/example/rewardsrader/data/local/Migrations.kt`.
- Bumped the database to v21 and wired the migration in `app/src/main/java/com/example/rewardsrader/data/local/AppDatabase.kt`, `app/src/main/java/com/example/rewardsrader/AppContainer.kt`, and `app/src/main/java/com/example/rewardsrader/data/worker/TrackerRefreshWorker.kt`.
- Updated benefit category parsing to map `restaurant` into Dining in `app/src/main/java/com/example/rewardsrader/ui/benefitcreate/BenefitCreateViewModel.kt`.
- Updated schema docs to drop `Restaurant` in `docs/schema/schema.prisma` and `docs/schema/schema.md`.

## 2026-01-18 - Card detail delete action
- Added a delete button to the card detail top bar and wired deletion via the viewmodel in `app/src/main/java/com/example/rewardsrader/ui/carddetail/CardDetailScreen.kt`, `app/src/main/java/com/example/rewardsrader/ui/carddetail/CardDetailViewModel.kt`, and `app/src/main/java/com/example/rewardsrader/MainActivity.kt`.
- Updated card detail UI tests for the new delete callback in `app/src/androidTest/java/com/example/rewardsrader/ui/CardScreensTest.kt`.

## 2026-01-19 - Offer status removal
- Removed the status field from the offer create/edit UI and wiring in `app/src/main/java/com/example/rewardsrader/ui/offercreate/OfferCreateScreen.kt` and `app/src/main/java/com/example/rewardsrader/MainActivity.kt`.

## 2026-01-19 - Offer status architecture removal
- Removed offer status from the Room entity and card detail UI in `app/src/main/java/com/example/rewardsrader/data/local/entity/OfferEntity.kt`, `app/src/main/java/com/example/rewardsrader/ui/carddetail/CardDetailViewModel.kt`, and `app/src/main/java/com/example/rewardsrader/ui/carddetail/components/BenefitOfferCards.kt`.
- Updated offer create/edit state and persistence to drop status in `app/src/main/java/com/example/rewardsrader/ui/offercreate/OfferCreateState.kt` and `app/src/main/java/com/example/rewardsrader/ui/offercreate/OfferCreateViewModel.kt`.
- Added migration 21->22 to remove the status column and bumped the DB to v22 in `app/src/main/java/com/example/rewardsrader/data/local/Migrations.kt` and `app/src/main/java/com/example/rewardsrader/data/local/AppDatabase.kt`.
- Wired the migration in `app/src/main/java/com/example/rewardsrader/AppContainer.kt` and `app/src/main/java/com/example/rewardsrader/data/worker/TrackerRefreshWorker.kt`, updated tests in `app/src/test/java/com/example/rewardsrader/data/CardRepositoryTest.kt`, and removed status from the schema docs (`docs/schema/schema.prisma`, `docs/schema/schema.md`) and `docs/feature_implementation.md`.

## 2026-01-19 - Card detail offer filtering
- Filtered card detail offers to hide those with completed trackers (manual completion or full usage) in `app/src/main/java/com/example/rewardsrader/ui/carddetail/CardDetailViewModel.kt`.

## 2026-01-19 - Tracker active cards and last four
- Tracker list now formats card names with last four digits when present and filters tracker data to active cards only in `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerViewModel.kt` and `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerUtils.kt`.
- Tracker detail header uses the same card name formatting in `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerEditViewModel.kt`.

## 2026-01-19 - Benefit frequency anniversary
- Added the `everyanniversary` option to the benefit frequency selector in `app/src/main/java/com/example/rewardsrader/ui/benefitcreate/BenefitCreateScreen.kt`.

## 2026-01-19 - SUB tracker
- Added SUB tracker generation and display using profile card sub-spending/duration in `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerGenerator.kt`, `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerViewModel.kt`, and `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerEditViewModel.kt`.
- Extended tracker source enum to include SUB and updated schema docs in `app/src/main/java/com/example/rewardsrader/data/local/entity/Enums.kt`, `docs/schema/schema.prisma`, and `docs/schema/schema.md`.
- Documented SUB as a tracker data source in `docs/feature_implementation.md` and added a card display name formatter in `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerUtils.kt`.

## 2026-01-19 - Tracker grouping by type
- Grouped tracker lists per card by type (SUB, Offers, Benefits) and hid SUB tracker titles in `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerScreen.kt`, `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerViewModel.kt`, and `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerEditViewModel.kt`.
- Suppressed empty tracker titles in the tracker detail summary card in `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerEditScreen.kt`.

## 2026-01-19 - SUB tracker header tweak
- Restored SUB tracker item titles and removed the SUB section header in `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerScreen.kt`, `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerViewModel.kt`, and `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerEditViewModel.kt`.

## 2026-01-19 - Sync button relocation
- Moved the sync action from the card list to the card creation screen in `app/src/main/java/com/example/rewardsrader/ui/cardlist/CardListScreen.kt`, `app/src/main/java/com/example/rewardsrader/ui/cardcreate/CardCreateScreen.kt`, and `app/src/main/java/com/example/rewardsrader/MainActivity.kt`.
- Updated the card list UI test signature in `app/src/androidTest/java/com/example/rewardsrader/ui/CardScreensTest.kt`.

## 2026-01-19 - Card create sync handling
- Moved Firestore sync logic into the card creation viewmodel and show sync feedback there in `app/src/main/java/com/example/rewardsrader/ui/cardcreate/CardCreateViewModel.kt` and `app/src/main/java/com/example/rewardsrader/ui/cardcreate/CardCreateScreen.kt`.
- Updated the card create viewmodel factory wiring in `app/src/main/java/com/example/rewardsrader/MainActivity.kt` and tests in `app/src/test/java/com/example/rewardsrader/ui/cardcreate/CardCreateViewModelTest.kt`.

## 2026-01-19 - Sync upsert safety
- Switched issuer/card/card-face inserts to Room upserts to avoid REPLACE deletes nulling `profile_cards.cardId` and `profile_cards.cardFaceId` during sync (`app/src/main/java/com/example/rewardsrader/data/local/dao/IssuerDao.kt`, `app/src/main/java/com/example/rewardsrader/data/local/dao/CardDao.kt`, `app/src/main/java/com/example/rewardsrader/data/local/dao/CardFaceDao.kt`).

## 2026-01-19 - Card add behavior clarification
- Reviewed template import flow and schema docs to confirm card vs benefit persistence when creating a profile card.
- No code changes; answered that profile cards reference `cards` while benefits are copied into new `benefits` rows linked via `profile_card_benefits`.

## 2026-01-19 - Benefit date update fix
- Fixed benefit edit persistence so updating start/end dates no longer deletes the linked benefit by switching the update path to use a SQL UPDATE instead of REPLACE (`app/src/main/java/com/example/rewardsrader/data/local/repository/CardRepository.kt`).

## 2026-01-19 - Card nickname default
- Stopped copying card product names into profile card nicknames during creation so the nickname field starts empty (`app/src/main/java/com/example/rewardsrader/template/CardTemplateImporter.kt`).

## 2026-01-19 - Card list nickname display
- Card list now shows nickname when present and falls back to product name when nickname is blank (`app/src/main/java/com/example/rewardsrader/ui/cardlist/CardListViewModel.kt`).

## 2026-01-19 - Last four input constraints
- Added numeric-only, max-6 input filtering for the card detail last-four field and enforced digit-only persistence (`app/src/main/java/com/example/rewardsrader/ui/carddetail/CardDetailScreen.kt`, `app/src/main/java/com/example/rewardsrader/ui/carddetail/components/EditDialogs.kt`, `app/src/main/java/com/example/rewardsrader/ui/carddetail/CardDetailViewModel.kt`).

## 2026-01-19 - Annual fee prefix
- Added a dollar prefix to the annual fee edit dialog to make the currency explicit (`app/src/main/java/com/example/rewardsrader/ui/carddetail/components/EditDialogs.kt`, `app/src/main/java/com/example/rewardsrader/ui/carddetail/CardDetailScreen.kt`).

## 2026-01-19 - Annual fee formatting
- Annual fee display now trims trailing `.0` for whole numbers in card detail (`app/src/main/java/com/example/rewardsrader/ui/carddetail/CardDetailViewModel.kt`).

## 2026-01-19 - Statement cut picker
- Replaced the statement cut date picker with a day-of-month selector and display strings like “1st of the month” (`app/src/main/java/com/example/rewardsrader/ui/carddetail/CardDetailScreen.kt`, `app/src/main/java/com/example/rewardsrader/ui/carddetail/components/EditDialogs.kt`).

## 2026-01-19 - Statement cut list scrolling
- Made the statement cut day list scrollable with a capped dialog height (`app/src/main/java/com/example/rewardsrader/ui/carddetail/components/EditDialogs.kt`).

## 2026-01-19 - Statement cut label
- Renamed the card detail label to “Statement / Closing date” (`app/src/main/java/com/example/rewardsrader/ui/carddetail/tabs/CardInfoTab.kt`).

## 2026-01-19 - Notes row spacing
- Increased spacing between labels and values and top-aligned the Notes label for long values (`app/src/main/java/com/example/rewardsrader/ui/carddetail/components/InfoRows.kt`, `app/src/main/java/com/example/rewardsrader/ui/carddetail/tabs/CardInfoTab.kt`).

## 2026-01-19 - Notes value alignment
- Left-aligned the Notes value while keeping other fields right-aligned (`app/src/main/java/com/example/rewardsrader/ui/carddetail/components/InfoRows.kt`, `app/src/main/java/com/example/rewardsrader/ui/carddetail/tabs/CardInfoTab.kt`).

## 2026-01-19 - Edit dialog keyboard focus
- Added auto-focus to edit dialogs for nickname, last-four, and annual fee, plus Notes dialog focus/keyboard display (`app/src/main/java/com/example/rewardsrader/ui/carddetail/components/EditDialogs.kt`, `app/src/main/java/com/example/rewardsrader/ui/carddetail/CardDetailScreen.kt`, `app/src/main/java/com/example/rewardsrader/ui/carddetail/tabs/CardInfoTab.kt`).

## 2026-01-19 - Card list snackbar host
- Moved card list snackbar handling into the main scaffold and pass visibility to adjust list/FAB spacing (`app/src/main/java/com/example/rewardsrader/MainActivity.kt`, `app/src/main/java/com/example/rewardsrader/ui/cardlist/CardListScreen.kt`, `app/src/androidTest/java/com/example/rewardsrader/ui/CardScreensTest.kt`).

## 2026-01-19 - Tracker period generation
- Adjusted tracker generation to create only the current period for benefits/offers so future trackers are created when each period starts (`app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerGenerator.kt`).

## 2026-01-21 - Tracker local notifications
- Added notification scheduling persistence (`NotificationScheduleEntity`, DAO, enum) with migration 22->23 and DB version bump (`app/src/main/java/com/example/rewardsrader/data/local/*`).
- Implemented tracker reminder scheduling with AlarmManager (exact when allowed, inexact fallback), receiver handling, and boot rescheduling (`app/src/main/java/com/example/rewardsrader/notifications/*`).
- Wired tracker reminder updates through the worker refresh and tracker detail UI, including permission prompts and 1-7 day lead time selection (`app/src/main/java/com/example/rewardsrader/data/worker/TrackerRefreshWorker.kt`, `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerEdit*`).
- Added tracker deep link handling and registered receivers/permissions in the manifest (`app/src/main/java/com/example/rewardsrader/MainActivity.kt`, `app/src/main/AndroidManifest.xml`).
- Updated schema docs to include notification schedules (`docs/schema/schema.prisma`, `docs/schema/schema.md`).

## 2026-01-21 - Tracker reminders list
- Updated notification schedules to allow multiple reminders per tracker with migration 23->24 and DB version bump (`app/src/main/java/com/example/rewardsrader/data/local/Migrations.kt`, `app/src/main/java/com/example/rewardsrader/data/local/AppDatabase.kt`).
- Reworked tracker reminder scheduling to manage per-reminder alarms and cancel all reminders when trackers become inactive (`app/src/main/java/com/example/rewardsrader/notifications/TrackerReminderScheduler.kt`).
- Replaced the reminder toggle with a reminders list UI that supports add/remove actions and permission prompts (`app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerEditScreen.kt`, `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerEditViewModel.kt`, `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerEditState.kt`).

## 2026-01-22 - Tracker detail UI refresh
- Removed section card backgrounds and adjusted tracker summary typography/amount formatting in the tracker detail screen (`app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerEditScreen.kt`).
- Updated reminders section layout to use a two-column icon + list layout with the add button anchored at the end of the list (`app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerEditScreen.kt`).
- Aligned the reminders icon with the list/add button layout for empty vs populated lists (`app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerEditScreen.kt`).
- Reworked the tracker notes area into an icon + content row with modal editing (`app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerEditScreen.kt`).
- Styled the Add reminder and Add notes placeholders with a muted grey tone (`app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerEditScreen.kt`).

## 2026-01-22 - Reminder selection UI
- Removed default pre-selection in the reminder timing dialog (`app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerEditScreen.kt`).
- Prevented duplicate reminder offsets from being added in the tracker detail flow (`app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerEditViewModel.kt`).
- Disabled already-added reminder offsets in the dialog and avoided double selection triggers (`app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerEditScreen.kt`).

## 2026-01-22 - Offer notes autosave
- Removed the bottom save button in tracker detail and persisted offer completion/notes changes automatically (`app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerEditScreen.kt`, `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerEditViewModel.kt`, `app/src/main/java/com/example/rewardsrader/MainActivity.kt`).
- Moved "Mark offer complete" to a right-aligned text button at the bottom of offer trackers and navigates back to the tracker list on click (`app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerEditScreen.kt`, `app/src/main/java/com/example/rewardsrader/MainActivity.kt`).
- Updated the offer completion button label and behavior to toggle between active/complete states (`app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerEditScreen.kt`, `app/src/main/java/com/example/rewardsrader/MainActivity.kt`).
- Added a filled background color to the offer completion text button (`app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerEditScreen.kt`).
- Rendered the offer completion action as a filled button so the background is always visible (`app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerEditScreen.kt`).
