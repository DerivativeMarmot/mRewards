## Files added/used (Step 1)
- `docs/config_schema.md`: Defines the JSON contract for card/benefit templates (required fields, enums, IDs, USD amounts, UTC date format `MM/dd/yyyy hh:mm`).
- `docs/sample_card_config.json`: Example data bundle implementing the schema (one card, two benefits).
- `docs/implementation_plan.md`: Step-by-step plan; Step 1 completed with schema and sample.
- `docs/product_functionality.md`: Product scope and entities; used as the source for schema fields.
- `AGENTS.md`: Contributor rules (read product/architecture docs before coding, modularization).

## Files added/used (Step 2)
- `app/src/main/java/com/example/rewardsrader/config/CardConfigModels.kt`: Serialization models and enums for the config payload (cards, benefits, enums).
- `app/src/main/java/com/example/rewardsrader/config/CardConfigParser.kt`: Parses JSON with validation (schema/data version non-empty, date format `MM/dd/yyyy hh:mm` UTC, enums, non-negative IDs, card/benefit cross-refs). Returns Success/Failure with errors.
- `app/src/main/java/com/example/rewardsrader/config/CardConfigLoader.kt`: Reads bundled asset `card_config.json` and delegates to the parser with error surfacing.
- `app/src/main/assets/card_config.json`: Bundled default config snapshot aligned to the schema.
- `app/src/test/java/com/example/rewardsrader/config/CardConfigParserTest.kt`: Unit tests for parser success and failure paths.

## Files added/used (Step 3)
- `app/src/main/java/com/example/rewardsrader/data/local/entity/*.kt`: Room entities for cards, applications, benefits, usage entries, notification rules; `TemplateCardWithBenefits` relation for joins.
- `app/src/main/java/com/example/rewardsrader/data/local/dao/*.kt`: DAOs for cards, benefits, applications, usage entries, notification rules.
- `app/src/main/java/com/example/rewardsrader/data/local/AppDatabase.kt`: Room database wiring DAOs and entities.
- `app/src/main/java/com/example/rewardsrader/data/local/repository/CardRepository.kt`: Repository to add a card with benefits, list, join, and remove.
- `app/src/test/java/com/example/rewardsrader/data/CardRepositoryTest.kt`: In-memory Room test (Robolectric) covering add/list/join/remove behavior.

## Files added/used (Step 4)
- `app/src/main/java/com/example/rewardsrader/data/local/entity/CardEntity.kt`: Added `statementCutUtc` and `welcomeOfferProgress`.
- `app/src/main/java/com/example/rewardsrader/data/local/AppDatabase.kt`: Version bumped to 2 to reflect entity change.
- `app/src/main/java/com/example/rewardsrader/data/local/repository/CardRepository.kt`: Extended to accept `ApplicationDao` and to insert application details when adding cards from templates.
- `app/src/main/java/com/example/rewardsrader/template/CardTemplateImporter.kt`: Maps selected config templates to Room entities and persists card + benefits + application.
- `app/src/test/java/com/example/rewardsrader/template/CardTemplateImporterTest.kt`: Robolectric test validating import flow and persistence of user-provided fields.

## Files added/used (Step 5)
- `app/src/main/java/com/example/rewardsrader/AppContainer.kt`: Simple DI container to build Room DB and `CardRepository`.
- `app/src/main/java/com/example/rewardsrader/ui/cardlist/CardListViewModel.kt`: Loads card summaries; exposes loading/error/empty states.
- `app/src/main/java/com/example/rewardsrader/ui/cardlist/CardListScreen.kt`: Compose list UI with state handling and item selection.
- `app/src/main/java/com/example/rewardsrader/ui/carddetail/CardDetailViewModel.kt`: Loads a card with benefits and applications into UI-friendly models.
- `app/src/main/java/com/example/rewardsrader/ui/carddetail/CardDetailScreen.kt`: Compose detail UI showing card metadata, application timeline, and benefits.
- `app/src/main/java/com/example/rewardsrader/MainActivity.kt`: Hosts navigation between list and detail using viewmodels from `AppContainer`.
- `app/src/androidTest/java/com/example/rewardsrader/ui/CardScreensTest.kt`: UI tests validating list/detail rendering with provided state.

## Files added/used (Step 5.1)
- `app/src/main/java/com/example/rewardsrader/ui/cardcreate/CardCreateState.kt`: State for issuer/product selection and user fields.
- `app/src/main/java/com/example/rewardsrader/ui/cardcreate/CardCreateViewModel.kt`: Loads issuers/templates from config provider, updates selections, saves via importer, resets form on success.
- `app/src/main/java/com/example/rewardsrader/ui/cardcreate/CardCreateScreen.kt`: Compose screen with modal dialogs for issuer/product selection, date pickers for open/statement dates, status selection dialog, and form fields.
- `app/src/main/java/com/example/rewardsrader/config/CardConfigProvider.kt`: Abstraction over config loader used by the create flow.
- `app/src/main/java/com/example/rewardsrader/template/CardTemplateImporter.kt`: Implements importer contract used by the create flow.
- `app/src/test/java/com/example/rewardsrader/ui/cardcreate/CardCreateViewModelTest.kt`: Unit test with fakes for config and importer; asserts load/reset behavior.
- `app/src/main/assets/card_config.json`: Expanded sample config with multiple issuers/products, all sharing sample benefits.

## Files added/used (Card editing enhancements)
- `app/src/main/java/com/example/rewardsrader/data/local/entity/CardEntity.kt`: Added `nickname`, `lastFour`.
- `app/src/main/java/com/example/rewardsrader/data/local/Migrations.kt`: Room migrations 1→2 and 2→3 (statementCut/welcomeOffer; nickname/lastFour).
- `app/src/main/java/com/example/rewardsrader/data/local/AppDatabase.kt`: DB version bumped to 3.
- `app/src/main/java/com/example/rewardsrader/data/local/dao/CardDao.kt`: Added getById/update.
- `app/src/main/java/com/example/rewardsrader/data/local/repository/CardRepository.kt`: Exposes getCard/updateCard.
- `app/src/test/java/com/example/rewardsrader/data/CardRepositoryTest.kt`: Covers update with nickname/lastFour.
- `app/src/main/java/com/example/rewardsrader/ui/carddetail/CardDetailViewModel.kt` / `CardDetailScreen.kt`: Show nickname/last4 when present.
- `app/src/main/java/com/example/rewardsrader/ui/cardedit/CardEditState.kt`, `CardEditViewModel.kt`, `CardEditScreen.kt`: Edit flow with nickname/last4, date/status pickers, and app bar back/save icons.
- `app/src/main/java/com/example/rewardsrader/MainActivity.kt`: BackHandler for in-app navigation; routes to edit screen via detail.
- `app/src/main/java/com/example/rewardsrader/data/local/Migrations.kt`: Included in `AppContainer` to wire migrations on DB build.
- Card detail top bar now icon-only; edit screen shows read-only product name.
- `app/src/main/java/com/example/rewardsrader/ui/cardlist/CardListViewModel.kt`: Supports delete with undo via snapshot of related data and snackbar state.
- `app/src/main/java/com/example/rewardsrader/ui/cardlist/CardListScreen.kt`: Delete icon per card, snackbar handling for undo.
- `app/src/main/java/com/example/rewardsrader/ui/cardlist/DeletedCardSnapshot.kt`: Captures card/benefits/applications/usage/notification rules for undo restore.
- DAOs/repos extended for bulk inserts/queries to support undo (`UsageEntryDao`, `NotificationRuleDao`, `ApplicationDao`, `CardDao`, `CardRepository`).

## Files added/used (Benefit creation UI)
- `app/src/main/java/com/example/rewardsrader/ui/benefitcreate/BenefitCreateState.kt`, `BenefitCreateViewModel.kt`, `BenefitCreateScreen.kt`, `CategoryItem.kt`: Benefit add flow with modal selectors (type/frequency radio), issuer-scoped categories (common + custom via edit modal), date pickers, notes; merchant removed.
- `app/src/main/java/com/example/rewardsrader/MainActivity.kt`: AddBenefit navigation wired; passes product/issuer; back handler updated earlier.
- `app/src/main/java/com/example/rewardsrader/ui/carddetail/CardDetailScreen.kt`: Benefits header shows add icon for navigating to AddBenefit.

## Files updated (Benefit UI polish)
- `ui/benefitcreate/BenefitCreateScreen.kt`: Selection rows now open radio-button dialogs with tappable rows; cadence label fixed to “annually”; dividers run edge-to-edge; categories header uses edit icon to open modal.
- `ui/benefitcreate/BenefitCreateViewModel.kt`: Custom categories are scoped per issuer, persist after deselection, and can be removed via modal; toggling chips no longer drops custom entries across issuers.

## 2025-12-27 – Files updated (Benefit deletion)
- `app/src/main/java/com/example/rewardsrader/data/local/dao/BenefitDao.kt`: Added `deleteById`.
- `app/src/main/java/com/example/rewardsrader/data/local/repository/CardRepository.kt`: Exposes `deleteBenefit`.
- `app/src/main/java/com/example/rewardsrader/ui/carddetail/CardDetailViewModel.kt`: Maps benefit IDs into UI, tracks current card, and deletes benefits with reload and error surfacing.
- `app/src/main/java/com/example/rewardsrader/ui/carddetail/CardDetailScreen.kt`: Replaced swipe delete attempts with a simple trailing trash icon per benefit card; tap triggers deletion.
- `app/src/main/java/com/example/rewardsrader/MainActivity.kt`: Passes delete callback to detail screen.
- `app/src/androidTest/java/com/example/rewardsrader/ui/CardScreensTest.kt`: Updated BenefitUi to carry IDs and screen call signatures.

## 2025-12-27 – Files updated (Benefit creation title/reset)
- `app/src/main/java/com/example/rewardsrader/ui/benefitcreate/BenefitCreateState.kt`: Added `title`.
- `app/src/main/java/com/example/rewardsrader/ui/benefitcreate/BenefitCreateViewModel.kt`: Stores title on benefits, clears form fields to defaults after save.
- `app/src/main/java/com/example/rewardsrader/ui/benefitcreate/BenefitCreateScreen.kt`: Title text field added.
- `app/src/main/java/com/example/rewardsrader/MainActivity.kt`: Wires title change into the benefit create screen.
- `app/src/main/java/com/example/rewardsrader/ui/carddetail/CardDetailViewModel.kt`: Includes title in `BenefitUi`; detail screen renders it when present.

## 2025-12-27 – Files updated (Card detail tabs/scroll)
- `app/src/main/java/com/example/rewardsrader/ui/carddetail/CardDetailScreen.kt`: Added card-face header and tab row (Card Info, Signup Bonus placeholder, Benefits, Offers placeholder); moved metadata/app timeline to Card Info tab; benefits tab uses existing list; converted layout to a single LazyColumn so header/tabs scroll together.
- `app/src/androidTest/java/com/example/rewardsrader/ui/CardScreensTest.kt`: Adjusted test to tap Benefits tab before assertions.

## 2025-12-27 – Files updated (Inline edits, date pickers, notes/status)
- `app/src/main/java/com/example/rewardsrader/ui/carddetail/CardDetailViewModel.kt`: Inline field updates now mutate in-memory detail state instead of reloading; tracks current card.
- `app/src/main/java/com/example/rewardsrader/ui/carddetail/CardDetailScreen.kt`: Inline editable rows with shared dividers, radio-button status modal with capitalized labels, borderless notes row with icon/value layout, date pickers for open/statement dates, removed top-bar edit icon.
- `app/src/main/java/com/example/rewardsrader/MainActivity.kt`: Simplified detail navigation (removed edit screen) to rely on inline edits.
- `app/src/main/java/com/example/rewardsrader/ui/cardedit/CardEditScreen.kt`: Removed in favor of inline edits.

## 2025-12-28 – Benefit edit bottom sheet and Material date pickers
- `app/src/main/java/com/example/rewardsrader/data/local/dao/BenefitDao.kt`: Added `getById` and `update` to support editing benefits.
- `app/src/main/java/com/example/rewardsrader/data/local/repository/CardRepository.kt`: Exposes benefit fetch/update helpers.
- `app/src/main/java/com/example/rewardsrader/ui/benefitcreate/BenefitCreateState.kt`: Tracks benefitId, edit mode flags, and metadata for editing.
- `app/src/main/java/com/example/rewardsrader/ui/benefitcreate/BenefitCreateViewModel.kt`: New `startEdit` loads an existing benefit and switches save to update; preserves custom categories.
- `app/src/main/java/com/example/rewardsrader/ui/benefitcreate/BenefitCreateScreen.kt`: Bottom sheet header shows Add vs Edit; Material 3 date pickers with inline label/value rows; type/frequency/date rows are full-row tappable.
- `app/src/main/java/com/example/rewardsrader/ui/carddetail/CardDetailScreen.kt`: Benefit cards are clickable with edit icons; benefits tab passes edit callbacks.
- `app/src/main/java/com/example/rewardsrader/MainActivity.kt`: Unifies add/edit benefit into a single bottom sheet with modes; back handling closes the sheet; wires edit callbacks and reloads detail on save.

## 2025-12-28 – Transactions, progress, SUB persistence
- `app/src/main/java/com/example/rewardsrader/data/local/entity/BenefitEntity.kt`: Added `transactionsJson` for multiplier transactions with migration to DB v5 and AppContainer wiring.
- `app/src/main/java/com/example/rewardsrader/data/local/entity/CardEntity.kt`: Added SUB fields (`subSpendingUsd`, `subDuration`, `subDurationUnit`); migrations bumped DB to v5.
- `app/src/main/java/com/example/rewardsrader/ui/benefitcreate/*`: Transaction modal (amount/date) with edit/delete/show-more, numeric inputs, 2-decimal trimming, multiplier-only progress bar vs cap.
- `app/src/main/java/com/example/rewardsrader/ui/carddetail/CardDetailViewModel.kt`: Maps/saves SUB fields; benefit mapping simplified to amount/rate + title; updates detail state on SUB changes.
- `app/src/main/java/com/example/rewardsrader/ui/carddetail/CardDetailScreen.kt`: Benefits tab uses FAB; transaction list shows edit/delete/show-more and progress bar; SUB tab mirrors info rows with numeric dialogs and months/days dropdown unit.
- `app/src/main/java/com/example/rewardsrader/MainActivity.kt`: Wires transaction callbacks and SUB update callbacks; DB migrations registered.

## 2026-01-01 – Offers & card detail restructure
- Data: Added `OfferEntity`, `OfferDao`, migration `MIGRATION_6_7`, DB bumped to v7, repository hooks for get/add/update/delete offers, and wiring in `AppContainer`.
- UI: Offer create/edit flow (`ui/offercreate/*`) with multiplier field, min/max cash back, status, dates, recommended spend hint; navigation wired in `MainActivity`.
- Card detail: split shared UI into components (`ui/carddetail/components/*`) and tabs (`ui/carddetail/tabs/*`); `CardDetailScreen.kt` now uses `LazyColumn` with sticky `TabRow` + `HorizontalPager` (min height to keep swipe active) to avoid nested scroll crashes and keep FAB per tab; offers tab lists add/edit/delete items.
## 2026-01-06 Firestore sync button
- `app/src/main/java/com/example/rewardsrader/data/remote/FirestoreSyncer.kt`: Pulls `issuers` and `cards` collections from Firestore (doc ID as primary key) and upserts into Room via repository; tolerates camel/underscore field names.
- `app/src/main/java/com/example/rewardsrader/AppContainer.kt`: Wires `FirebaseFirestore` into `FirestoreSyncer` for dependency injection.
- `app/src/main/java/com/example/rewardsrader/ui/cardlist/CardListViewModel.kt`: Adds cloud sync trigger dispatched on IO with success snackbar and reload.
- `app/src/main/java/com/example/rewardsrader/ui/cardlist/CardListScreen.kt`: Adds sync icon in the top bar.
- `app/src/main/java/com/example/rewardsrader/MainActivity.kt`: Passes sync callback into the card list screen.

## 2026-01-06 Card creation from local DB
- `app/src/main/java/com/example/rewardsrader/data/local/repository/CardRepository.kt`: Implements `CardTemplateSource` with issuer/card getters and exposes `getTemplateCardWithBenefits` for template lookups.
- `app/src/main/java/com/example/rewardsrader/template/CardTemplateImporter.kt`: New `importFromDatabase` maps an existing template card (and benefits, when present) into a profile card/application without config.
- `app/src/main/java/com/example/rewardsrader/ui/cardcreate/CardCreateState.kt` / `CardCreateViewModel.kt` / `CardCreateScreen.kt`: Issuer/product options now load from Room (string IDs), dialogs filter by issuer, and saves use the DB importer; state tracks issuer/card options instead of config templates.
- `app/src/main/java/com/example/rewardsrader/MainActivity.kt`: Card creation wiring updated to pass the repository into the viewmodel factory.
- `app/src/test/java/com/example/rewardsrader/ui/cardcreate/CardCreateViewModelTest.kt`: Adjusted to stub DB-backed issuer/card sources and the new importer entry point.

## 2026-01-11 тА?Card creation dialog scroll
- `app/src/main/java/com/example/rewardsrader/ui/cardcreate/CardCreateScreen.kt`: Issuer/product selection dialogs now use a bounded, scrollable list so long catalogs remain accessible.

## 2026-01-11 тА?Profile card face reference
- `docs/schema/schema.prisma`, `docs/schema/schema.md`: Added `cardFaceId` on `ProfileCard` with relation/index to `CardFace`.
- `app/src/main/java/com/example/rewardsrader/data/local/entity/ProfileCardEntity.kt`: New optional `cardFaceId` column with FK to `card_faces` and index.
- `app/src/main/java/com/example/rewardsrader/data/local/entity/ProfileRelations.kt`: Exposes related `CardFaceEntity` on profile card relations.
- `app/src/main/java/com/example/rewardsrader/data/local/Migrations.kt`: Migration 16тЖ?7 rebuilds `profile_cards` with `cardFaceId` FK and indexes.
- `app/src/main/java/com/example/rewardsrader/data/local/AppDatabase.kt`, `AppContainer.kt`: Bumped DB to v17 and wired the new migration.
- `app/src/main/java/com/example/rewardsrader/data/local/dao/CardFaceDao.kt`, `CardRepository.kt`, `template/CardTemplateImporter.kt`: Card face DAO now exposes preferred face lookup; importer copies the preferred/default card face ID into new profile cards when created.

## 2026-01-11 тА?Card face display
- `gradle/libs.versions.toml`, `app/build.gradle.kts`: Added Coil Compose for remote image loading.
- `app/src/main/java/com/example/rewardsrader/ui/carddetail/CardDetailViewModel.kt`: Card detail UI model includes card face URL from related card face.
- `app/src/main/java/com/example/rewardsrader/ui/carddetail/CardDetailScreen.kt`: Header now renders the card face from `remoteUrl` (or fallback text) using `AsyncImage` at larger height without issuer/product labels.

## 2026-01-11 тА?Firestore card face sync
- `app/src/main/java/com/example/rewardsrader/data/remote/FirestoreSyncer.kt`: Sync now pulls `card_faces`, maps defaults/URLs, and upserts via repository with result counts including faces.

## 2026-01-11 тА?Card face picker
- `app/src/main/java/com/example/rewardsrader/data/local/repository/CardRepository.kt`: Exposes card face list fetch.
- `app/src/main/java/com/example/rewardsrader/ui/carddetail/CardDetailViewModel.kt`: Loads card face options, maps them into state, and updates selection to `cardFaceId` with URL refresh.
- `app/src/main/java/com/example/rewardsrader/ui/carddetail/CardDetailScreen.kt`: Top bar menu with "Switch card face" opens a bottom sheet listing faces for selection; selection saves to the profile card and updates the header face.
- `app/src/main/java/com/example/rewardsrader/MainActivity.kt`: Wires face selection callback to the detail viewmodel.

## 2026-01-12 - Card creation search plan
- `docs/feature_implementation.md`: Added an implementation plan to turn the card creation screen into a search-first experience with a result list, sort/filter bottom sheets, and selection flow into the existing detail form; includes state, layout, and testing notes.
- Plan refined: selecting a search result now immediately creates the card via the importer, defaulting open date to today (stored UTC, displayed local) and status to `pending`, leaving optional fields blank; success navigates back to the Card List with snackbar feedback, failures stay on search with an error snackbar.

## 2026-01-12 - Card creation search UI
- `app/src/main/java/com/example/rewardsrader/ui/cardcreate/CardCreateState.kt`: Reframed state around search results, filters (issuer/network/segment/payment instrument/benefit type/category, annual fee range, no-fee toggle), sort mode, and query text.
- `app/src/main/java/com/example/rewardsrader/ui/cardcreate/CardCreateViewModel.kt`: Loads issuers/cards with benefits, builds filter metadata, applies query/sort/filter to derive result lists, and emits events on immediate creation (open date in UTC, status pending, optional fields blank) with error/success handling.
- `app/src/main/java/com/example/rewardsrader/ui/cardcreate/CardCreateScreen.kt`: New search-first UI with search bar, sort/filter chips and bottom sheets, result list cards, and snackbar handling; tapping a card triggers immediate import and navigation callback.
- `app/src/main/java/com/example/rewardsrader/ui/cardlist/CardListViewModel.kt`: Added `notifyCardAdded` to surface a snackbar when returning from create.
- `app/src/main/java/com/example/rewardsrader/data/local/dao/TemplateCardDao.kt`, `.../repository/CardRepository.kt`: Expose template cards with benefits for filter metadata.
- `app/src/main/java/com/example/rewardsrader/MainActivity.kt`: Wired new create-screen callbacks/events and success navigation back to the card list.
- `app/src/test/java/com/example/rewardsrader/ui/cardcreate/CardCreateViewModelTest.kt`: Updated for search-first flow and creation event assertion.
- UI tweak: filter bottom sheet height capped to 85% to leave space below the top bar.
- Filter sheet content is scrollable to ensure all filter controls are reachable on smaller screens.
- Added a scrollbar indicator on the filter sheet to make scroll position visible.
- Issuer filter chips now render leading icons from drawable assets (amex/chase/citi/bofa/capital one/discover/barclays/hsbc/us bank) for quicker visual scanning.
- Card search list items redesigned: three rows with product title, a scaled-down default card face thumbnail plus annual fee, and benefit category chips; default face URLs are pulled when available with a text fallback otherwise.
- CardCreateScreen now supports a preview bottom sheet when tapping a search result, showing card name, full-size face, annual fee, and benefits (title/amount/cadence). A top-right plus icon on each list item triggers immediate card creation; row tap only opens the preview. Repository exposes preferred card face URL/ID for list/preview use.

## 2026-01-15 - Card list FAB
- `app/src/main/java/com/example/rewardsrader/ui/cardlist/CardListScreen.kt`: Moved the Add card action into the Scaffold floating action button and removed inline Add card buttons from list/empty states.

## 2026-01-15 - Card list snackbar/FAB spacing
- `app/src/main/java/com/example/rewardsrader/ui/cardlist/CardListScreen.kt`: Aligned the card list FAB/snackbar behavior with card detail by anchoring the FAB in content, shifting it upward when a snackbar is visible, and padding list content to stay clear of the FAB/snackbar.

## 2026-01-15 - Card list item layout
- `app/src/main/java/com/example/rewardsrader/ui/cardlist/CardListViewModel.kt`: Added card face URL, last four, and open date to the list UI model for richer card list rendering.
- `app/src/main/java/com/example/rewardsrader/ui/cardlist/CardListScreen.kt`: Redesigned card list rows with a cropped card-face strip, an overlapping name/last-four row with delete action, and an approval-duration plus status row.
- `app/src/androidTest/java/com/example/rewardsrader/ui/CardScreensTest.kt`: Updated card list/detail test fixtures to match the new list model and detail UI signatures.

## 2026-01-15 - Card list refresh on return
- `app/src/main/java/com/example/rewardsrader/ui/cardlist/CardListScreen.kt`: Added a lifecycle resume hook so the list refreshes when returning from detail.
- `app/src/main/java/com/example/rewardsrader/MainActivity.kt`: Wires the resume refresh to reload cards without showing the loading state.
- `app/src/androidTest/java/com/example/rewardsrader/ui/CardScreensTest.kt`: Updated the list screen call signature for the new resume callback.

## 2026-01-15 - Card list layout no overlap
- `app/src/main/java/com/example/rewardsrader/ui/cardlist/CardListScreen.kt`: Removed the overlapping layout so the card face strip and text rows render as separate stacked rows for long titles.

## 2026-01-16 - Card list long-press actions
- `app/src/main/java/com/example/rewardsrader/ui/cardlist/CardListScreen.kt`: Added a long-press overflow menu on card items with Duplicate/Delete actions via combinedClickable.
- `app/src/main/java/com/example/rewardsrader/ui/cardlist/CardListViewModel.kt`: Added duplicate action handling and snackbar feedback.
- `app/src/main/java/com/example/rewardsrader/data/local/repository/CardRepository.kt`: Implemented profile card duplication (card, benefits, notifications, offers, applications).
- `app/src/main/java/com/example/rewardsrader/MainActivity.kt`: Wired duplicate action into the list screen.
- `app/src/androidTest/java/com/example/rewardsrader/ui/CardScreensTest.kt`: Updated list screen call signature for the new duplicate callback.

## 2026-01-16 - Card list menu styling
- `app/src/main/java/com/example/rewardsrader/ui/cardlist/CardListScreen.kt`: Right-aligned the card list long-press menu and forced a white menu background with black text for clarity.

## 2026-01-16 - Card list menu icons
- `app/src/main/java/com/example/rewardsrader/ui/cardlist/CardListScreen.kt`: Added icons for Duplicate and Delete actions in the card list long-press menu.

## 2026-01-18 - Tracker feature
- `app/src/main/java/com/example/rewardsrader/data/local/entity/TrackerEntity.kt`: Stores tracker periods tied to profile benefits/offers.
- `app/src/main/java/com/example/rewardsrader/data/local/entity/TrackerTransactionEntity.kt`: Stores tracker-specific transactions.
- `app/src/main/java/com/example/rewardsrader/data/local/dao/TrackerDao.kt`: Reads/inserts tracker periods for profile cards.
- `app/src/main/java/com/example/rewardsrader/data/local/dao/TrackerTransactionDao.kt`: CRUD for tracker transaction entries.
- `app/src/main/java/com/example/rewardsrader/data/local/entity/Enums.kt`: Added `TrackerSourceType` to describe tracker sources.
- `app/src/main/java/com/example/rewardsrader/data/local/EnumConverters.kt`: Converts tracker source enums for Room storage.
- `app/src/main/java/com/example/rewardsrader/data/local/Migrations.kt`: Migration 17->18 creates tracker tables and indexes.
- `app/src/main/java/com/example/rewardsrader/data/local/AppDatabase.kt`: DB version bump and tracker DAO registration.
- `app/src/main/java/com/example/rewardsrader/data/local/repository/CardRepository.kt`: Tracker query/insert/update helpers for UI flows.
- `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerState.kt`: Tracker list state and UI models.
- `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerEditState.kt`: Tracker edit state and transaction UI models.
- `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerUtils.kt`: Shared date/amount formatting helpers for tracker UI.
- `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerViewModel.kt`: Builds tracker lists from profile benefits/offers with status logic.
- `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerEditViewModel.kt`: Loads tracker detail, manages transactions and offer completion.
- `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerScreen.kt`: Tracker list UI with filter chips for status.
- `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerEditScreen.kt`: Tracker edit UI for transactions and completion.
- `app/src/main/java/com/example/rewardsrader/MainActivity.kt`: Adds bottom navigation and tracker routes.
- `app/src/main/java/com/example/rewardsrader/AppContainer.kt`: Wires tracker DAOs and migration.
- `docs/schema/schema.prisma`: Documents tracker tables and enums.
- `docs/schema/schema.md`: Documents tracker tables and enums in markdown.

## 2026-01-18 - Tracker flash fix
- `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerViewModel.kt`: Added load options to refresh without toggling the loading state.
- `app/src/main/java/com/example/rewardsrader/MainActivity.kt`: Uses `LaunchedEffect` for tracker detail loading and refreshes tracker lists without flashing.

## 2026-01-18 - Tracker calendar periods
- `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerViewModel.kt`: Generates monthly/quarterly/semi-annual/annual trackers on calendar boundaries with partial first periods.

## 2026-01-18 - Offer tracker notes
- `app/src/main/java/com/example/rewardsrader/data/local/entity/TrackerEntity.kt`: Adds tracker notes to persist offer notes.
- `app/src/main/java/com/example/rewardsrader/data/local/Migrations.kt`: Migration 18->19 adds tracker notes column.
- `app/src/main/java/com/example/rewardsrader/data/local/AppDatabase.kt`: DB version bumped to 19.
- `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerEditViewModel.kt`: Saves offer completion + notes via tracker updates.
- `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerEditScreen.kt`: Hides transactions for offers and adds notes + save action.

## 2026-01-18 - Offer tracker save navigation
- `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerEditViewModel.kt`: Added save callback support for offer tracker updates.
- `app/src/main/java/com/example/rewardsrader/MainActivity.kt`: Navigates back to the tracker list after saving offer trackers.

## 2026-01-18 - Tracker add transaction modal
- `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerEditScreen.kt`: Adds a plus-action modal for benefit tracker transaction entry.

## 2026-01-18 - Tracker list grouping
- `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerScreen.kt`: Groups trackers by card and uses a two-row tracker item layout with amount usage and time left.

## 2026-01-18 - Tracker list spacing
- `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerScreen.kt`: Tightened spacing and rounded only the first/last tracker items in each card group.

## 2026-01-18 - Tracker list background
- `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerScreen.kt`: Uses the theme background color for tracker item cards.

## 2026-01-18 - Tracker card faces
- `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerScreen.kt`: Adds cropped card faces to tracker card groups and groups by profile card ID.
- `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerState.kt`: Carries profile card IDs and card face URLs in tracker UI models.

## 2026-01-18 - Tracker scroll restore
- `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerScreen.kt`: Remembers the list scroll state across navigation.

## 2026-01-18 - Tracker single item rounding
- `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerScreen.kt`: Fully rounds single tracker items inside card groups.

## 2026-01-18 - Tracker background refresh
- `app/src/main/java/com/example/rewardsrader/data/worker/TrackerRefreshWorker.kt`: Periodically generates trackers in the background.
- `app/src/main/java/com/example/rewardsrader/data/worker/TrackerWorkScheduler.kt`: Schedules periodic tracker refresh work.
- `app/src/main/java/com/example/rewardsrader/ui/tracker/TrackerGenerator.kt`: Shared tracker generation logic used by UI and worker.
- `app/src/main/java/com/example/rewardsrader/AppContainer.kt`: Schedules the tracker refresh when the app container initializes.

## 2026-01-18 - Card status options
- `app/src/main/java/com/example/rewardsrader/ui/carddetail/components/EditDialogs.kt`: Limits card status selection to Pending, Active, and Closed.

## 2026-01-18 - Card status save fix
- `app/src/main/java/com/example/rewardsrader/ui/carddetail/components/EditDialogs.kt`: Persists selected status without lowercasing enum values.
