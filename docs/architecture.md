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
- `app/src/main/java/com/example/rewardsrader/data/local/entity/*.kt`: Room entities for cards, applications, benefits, usage entries, notification rules; `CardWithBenefits` relation for joins.
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
