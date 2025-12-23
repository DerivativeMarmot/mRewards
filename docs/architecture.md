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
