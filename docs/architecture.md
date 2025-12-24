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
