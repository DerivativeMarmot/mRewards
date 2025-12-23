# Card Config Schema (JSON)

- Format: JSON; top-level keys: `schema_version` (string), `data_version` (string), `cards` (array), `benefits` (array).
- IDs: serial numbers (integers) for `card_id` and `benefit_id`; benefits reference `card_id`.
- Currency: USD only; amounts use plain numbers.
- Date/time: strings in `MM/dd/yyyy hh:mm` (12-hour clock, UTC) for storage; convert to local time for display.

## Card fields (required unless noted)
- `card_id` (int): serial identifier.
- `issuer` (string): bank/issuer name.
- `product_name` (string): public product name.
- `network` (string): payment network (e.g., Visa, MasterCard, Amex).
- `annual_fee_usd` (number): annual fee in USD.
- `last_updated` (string): UTC date/time `MM/dd/yyyy hh:mm`.
- `data_source` (string): maintainer or source label.
- `notes` (string, optional): special handling or regional notes.

## Benefit fields (required unless noted)
- `benefit_id` (int): serial identifier.
- `card_id` (int): FK to cards.
- `type` (enum): `credit` | `multiplier` | `access` | `offer`.
- `amount_usd` (number, optional): value for credits.
- `cap_usd` (number, optional): cap per cadence.
- `cadence` (enum): `once` | `monthly` | `quarterly` | `annual`.
- `category` (string, optional): spend category (e.g., dining, travel).
- `merchant` (string, optional): specific merchant/partner if applicable.
- `enrollment_required` (boolean): whether activation is needed.
- `effective_date` (string): UTC date/time `MM/dd/yyyy hh:mm`.
- `expiry_date` (string, optional): UTC date/time `MM/dd/yyyy hh:mm`.
- `terms` (string, optional): short text for limitations.
- `notes` (string, optional): extra maintainer notes.
