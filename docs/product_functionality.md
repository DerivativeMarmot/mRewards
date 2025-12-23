# RewardsRader – Product & Functionality Overview

## Purpose
- Help users track their credit cards, including application dates, statuses, and ongoing benefits (cash back, credits, points multipliers).
- Surface timely actions (e.g., spend thresholds, expiring credits) so users capture value and avoid missed benefits.
- Provide structured, maintained card data (fees, benefits, terms) from a centralized config.

## Target users and scenarios
- Applicants who want a single place to remember what they applied for and when.
- Cardholders who need reminders to use benefits before they expire.
- Deal seekers who compare cards and benefits based on up-to-date, maintained card data.

## Core features
- **Card vault**: Add/edit/remove cards with issuer, product, network, annual fee, open/close dates, application status, and notes. Support storing card images/logos (local or remote).
- **Application timeline**: Record application date, decision date, approval/denial, credit pull bureau, and reconsideration outcomes.
- **Benefits catalog**: For each card, store benefits such as monthly/quarterly credits, category multipliers, lounge access, partner offers, and key terms (caps, enrollment required, stacking rules).
- **Action reminders**: Remind users about expiring credits, annual fee posts, statement cut dates, and spend milestones; user-configurable notifications.
- **Usage tracker**: Mark benefits as used/redeemed (e.g., “$10 dining credit used in Jan”), track progress toward spend thresholds, and show remaining amounts by period.
- **Earnings & value summary**: Show totals by card/period (cash back, credits redeemed, points earned) and an estimated net value after annual fees.
- **Discovery/reference**: Browse a maintained catalog of cards/benefits from the centralized config, with filters and notes for special handling.
- **Search & filters**: Filter by issuer, category, fee level, benefit type, status (open/closed), and expiring soon.
- **Security**: Store only non-sensitive card info by default. If storing account identifiers, apply local encryption and biometric unlock where available.

## Card data source (centralized config)
- Maintain a versioned JSON (or YAML) data file that lists card products, annual fees, benefits, caps, cadences, categories, enrollment requirements, and effective/expiry dates.
- Treat it as the single source of truth, updated frequently by maintainers; include schema versioning and change logs.
- Bundle a stable snapshot in the app; allow manual refresh from the maintained source when connectivity is available.
- Include metadata per entry: last_updated, data_source (maintainer), and optional notes for special handling (e.g., regional differences).
- When a user adds a card, the app pulls that card’s template from the config, pre-populates benefits/fees/terms, and prompts the user to fill personal fields (e.g., open date, statement cut, application status, spend progress).

## Key entities (draft)
- **Card**: id, issuer, product name, network, annual fee, open date, close date, status, notes.
- **Application**: card_id, application date, decision date, status, credit bureau, reconsideration notes, welcome offer terms.
- **Benefit**: card_id, type (credit/multiplier/access/etc.), amount/cap, cadence (monthly/quarterly/annual), category/merchant, enrollment_required, expiry, terms, data_source.
- **Usage entry**: benefit_id, date, amount/value used, proof/receipt link, location/merchant, notes.
- **Notification rule**: benefit_id, trigger (days-before-expiry, statement-cut, spend-remaining), channel (in-app, push), enabled flag.

## UX flows (high level)
- Onboarding: user adds first card; selects product from catalog (config), benefits/fees pre-populate, user fills personal details (open date, status, statement cut, welcome offer progress).
- Card detail: shows application timeline, active benefits with progress, upcoming expirations, and maintainer notes from the config.
- Add/mark benefit usage: quick add with suggested amount (remaining) and date defaults to today.
- Notifications: user can toggle reminders per benefit; in-app list of upcoming actions.
- Discovery: browse cards/benefits from the maintained catalog with filters; view notes for special handling.

## Assumptions and open questions
- Confirm final format for the centralized config (JSON vs YAML) and hosting/update mechanism.
- Determine locales/currencies to support beyond USD.
- Decide on authentication (if any) and whether to sync data across devices or keep it local-only.
- Confirm if storing any sensitive identifiers is required; default stance is to avoid storing full card numbers.

## Next steps
- Define the config schema (fields, enums, versioning) and hosting strategy.
- Define MVP scope (minimum cards/benefit types, notification types) and cut a milestone plan.
- Design data schema and storage approach (e.g., Room database) aligned to the entities above.
