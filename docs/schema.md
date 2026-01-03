# Database Schema

This document outlines the database schema for the Rewards Radar application.

## Overview

The application uses a local SQLite database (via Room) with the following entities. All primary keys and foreign keys use **String IDs** (NoSQL Object ID style, e.g., CUID or UUID) instead of auto-incrementing integers to support distributed generation and easier syncing in the future.

## Entities

### Card
Represents a credit card product added by the user.

| Field | Type | Description |
| :--- | :--- | :--- |
| `id` | String (PK) | Unique identifier for the card. |
| `issuer` | String | Bank or issuer name. |
| `productName` | String | Name of the card product. |
| `network` | String | Payment network (Visa, MC, Amex, etc.). |
| `annualFeeUsd` | Double | Annual fee in USD. |
| `status` | String | Status (e.g., Open, Closed). |
| `nickname` | String? | User-defined nickname. |
| `lastFour` | String? | Last 4 digits. |
| `openDateUtc` | String? | Account open date (UTC). |
| `closeDateUtc` | String? | Account close date (UTC). |
| `statementCutUtc` | String? | Statement cut date (UTC). |
| `welcomeOfferProgress` | String? | User's progress note for welcome offer. |
| `notes` | String? | User notes. |
| `subSpendingUsd` | Double? | Spending requirement for SUB. |
| `subDuration` | Int? | Duration for SUB. |
| `subDurationUnit` | String? | Unit for duration (default: "months"). |

### Benefit
Represents a specific benefit attached to a card (e.g., credit, multiplier).

| Field | Type | Description |
| :--- | :--- | :--- |
| `id` | String (PK) | Unique identifier for the benefit. |
| `cardId` | String (FK) | Reference to the `Card`. |
| `type` | String | Type (credit, multiplier, access, offer). |
| `amountUsd` | Double? | Dollar value (for credits). |
| `capUsd` | Double? | Maximum value/spend cap. |
| `cadence` | String | Frequency (monthly, annual, etc.). |
| `category` | String? | Spend category. |
| `merchant` | String? | Specific merchant. |
| `enrollmentRequired` | Boolean | If activation is required. |
| `effectiveDateUtc` | String | Start date (UTC). |
| `expiryDateUtc` | String? | End date (UTC). |
| `terms` | String? | Terms and conditions. |
| `dataSource` | String? | Source of data. |
| `notes` | String? | User/Maintainer notes. |
| `transactionsJson` | String? | JSON string of transactions (for multipliers). |

### Offer
Represents a temporary offer or promotion on a card.

| Field | Type | Description |
| :--- | :--- | :--- |
| `id` | String (PK) | Unique identifier for the offer. |
| `cardId` | String (FK) | Reference to the `Card`. |
| `title` | String | Offer title. |
| `type` | String | Offer type. |
| `status` | String | Status (default: active). |
| `note` | String? | Description. |
| `startDateUtc` | String? | Start date (UTC). |
| `endDateUtc` | String? | End date (UTC). |
| `multiplierRate` | Double? | Multiplier rate. |
| `minSpendUsd` | Double? | Minimum spend. |
| `maxCashBackUsd` | Double? | Maximum cash back. |

### Application
Represents the application history for a card.

| Field | Type | Description |
| :--- | :--- | :--- |
| `id` | String (PK) | Unique identifier for the application record. |
| `cardId` | String (FK) | Reference to the `Card`. |
| `status` | String | Application status. |
| `applicationDateUtc` | String? | Date applied (UTC). |
| `decisionDateUtc` | String? | Date decided (UTC). |
| `creditBureau` | String? | Credit bureau pulled. |
| `reconsiderationNotes` | String? | Notes on recon. |
| `welcomeOfferTerms` | String? | Terms of the welcome offer. |

### UsageEntry
Represents a usage event for a benefit (e.g., using a dining credit).

| Field | Type | Description |
| :--- | :--- | :--- |
| `id` | String (PK) | Unique identifier for the usage entry. |
| `benefitId` | String (FK) | Reference to the `Benefit`. |
| `dateUtc` | String | Date of usage (UTC). |
| `amountUsd` | Double? | Amount used/spent. |
| `notes` | String? | Notes. |
| `merchant` | String? | Merchant name. |
| `location` | String? | Location. |
| `proofLink` | String? | Link to receipt/proof. |

### NotificationRule
Represents a user-configured notification trigger for a benefit.

| Field | Type | Description |
| :--- | :--- | :--- |
| `id` | String (PK) | Unique identifier for the rule. |
| `benefitId` | String (FK) | Reference to the `Benefit`. |
| `trigger` | String | Trigger condition. |
| `channel` | String | Notification channel. |
| `enabled` | Boolean | Whether enabled. |
