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
