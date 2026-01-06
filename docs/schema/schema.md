# Database Schema

This document outlines the database schema for the Rewards Radar application, based on the `schema.prisma` file.

## Overview

The application uses a local SQLite database, and the schema is defined using Prisma. This document provides a detailed breakdown of each model and enum.

## Enums

### BenefitCategory
```prisma
enum BenefitCategory {
  Dining
  OnlineShopping
  Grocery
  Restaurant
  DrugStore
  Travel
  Gas
  EVCharging
  Streaming
  Transit
  Utilities
  Others
}
```

### BenefitFrequency
```prisma
enum BenefitFrequency {
  Monthly
  Quarterly
  SemiAnnually
  Annually
  EveryAnniversary
}
```

### BenefitType
```prisma
enum BenefitType {
  Credit
  Multiplier
}
```

### CardNetwork
```prisma
enum CardNetwork {
  Visa
  Mastercard
  Discover
  Amex
}
```

### PaymentInstrument
```prisma
enum PaymentInstrument {
  Credit
  Debit
  Charge
}
```

### CardSegment
```prisma
enum CardSegment {
  Personal
  Business
}
```

### CardStaus
```prisma
enum CardStaus {
  Active
  Closed
  Pending
}
```

### CardSubDurationUnit
```prisma
enum CardSubDurationUnit {
  Day
  Month
}
```

## Models

### Application
| Field | Type | Description |
| :--- | :--- | :--- |
| `id` | String | @id @default(cuid()) |
| `profileCardId` | String | @map("profile_card_id") |
| `applicationDateUtc` | String? | @map("application_date_utc") |
| `decisionDateUtc` | String? | @map("decision_date_utc") |
| `status` | String | |
| `creditBureau` | String? | @map("credit_bureau") |
| `reconsiderationNotes` | String? | @map("reconsideration_notes") |
| `welcomeOfferTerms` | String? | @map("welcome_offer_terms") |
| `profileCard` | ProfileCard | @relation(fields: [profileCardId], references: [id], onDelete: Cascade) |

### Benefit
| Field | Type | Description |
| :--- | :--- | :--- |
| `id` | String | @id @default(cuid()) @map("_id") |
| `type` | BenefitType | @default(Credit) |
| `amount` | Float? | @map("amount") |
| `cap` | Float? | @map("cap") |
| `frequency` | BenefitFrequency | |
| `category` | BenefitCategory[] | |
| `enrollmentRequired` | Boolean | @map("enrollment_required") |
| `startDateUtc` | String | @map("start_date_utc") |
| `endDateUtc` | String? | @map("end_date_utc") |
| `notes` | String? | |
| `transactions` | Transaction[] | |
| `notificationRules` | NotificationRule[] | |
| `cardBenefits` | CardBenefit[] | |
| `profileCardBenefits` | ProfileCardBenefit[] | |

### Card
| Field | Type | Description |
| :--- | :--- | :--- |
| `id` | String | @id, Stable ID from seed/JSON (e.g., "chase_sapphire_preferred") |
| `issuerId` | String | @map("issuer_id") |
| `productName` | String | @map("product_name") |
| `faces` | CardFace[] | |
| `network` | CardNetwork | @default(Visa) |
| `paymentInstrument` | PaymentInstrument | @default(Credit) @map("payment_instrument") |
| `segment` | CardSegment | @default(Personal) |
| `annualFee` | Float | @map("annual_fee") |
| `profileCards` | ProfileCard[] | |
| `foreignTransactionFee` | Float | @default(0.00) @map("foreign_transaction_fee") |
| `cardBenefits` | CardBenefit[] | |
| `issuer` | Issuer | @relation(fields: [issuerId], references: [id], onDelete: Cascade) |

### CardBenefit
| Field | Type | Description |
| :--- | :--- | :--- |
| `cardId` | String | @map("card_id") |
| `benefitId` | String | @map("benefit_id") |
| `card` | Card | @relation(fields: [cardId], references: [id], onDelete: Cascade) |
| `benefit` | Benefit | @relation(fields: [benefitId], references: [id], onDelete: Cascade) |

### CardFace
| Field | Type | Description |
| :--- | :--- | :--- |
| `id` | String | @id |
| `remoteUrl` | String |  |
| `localPath` | String? | |
| `isDefault` | Boolean | @default(false) is it the default card face of the card. |
| `card` | Card? | @relation(fields: [cardId], references: [id]) |
| `cardId` | String? | |

### Issuer
| Field | Type | Description |
| :--- | :--- | :--- |
| `id` | String | @id, Stable ID from seed/JSON (e.g., "chase", "amex") |
| `name` | String | |
| `cards` | Card[] | |

### NotificationRule
| Field | Type | Description |
| :--- | :--- | :--- |
| `id` | String | @id @default(cuid()) |
| `benefitId` | String | @map("benefit_id") |
| `trigger` | String | |
| `channel` | String | |
| `enabled` | Boolean | |
| `benefit` | Benefit | @relation(fields: [benefitId], references: [id], onDelete: Cascade) |

### Offer
| Field | Type | Description |
| :--- | :--- | :--- |
| `id` | String | @id @default(cuid()) |
| `profileCardId` | String | @map("profile_card_id") |
| `title` | String | |
| `note` | String? | |
| `startDateUtc` | String? | @map("start_date_utc") |
| `endDateUtc` | String? | @map("end_date_utc") |
| `type` | String | |
| `multiplierRate` | Float? | @map("multiplier_rate") |
| `minSpend` | Float? | @map("min_spend") |
| `maxCashBack` | Float? | @map("max_cash_back") |
| `status` | String | @default("active") |
| `profileCard` | ProfileCard | @relation(fields: [profileCardId], references: [id], onDelete: Cascade) |

### Profile
| Field | Type | Description |
| :--- | :--- | :--- |
| `id` | String | @id @default(cuid()) |
| `userId` | String? | |
| `name` | String | |
| `cards` | ProfileCard[] | |

### ProfileCard
| Field | Type | Description |
| :--- | :--- | :--- |
| `id` | String | @id @default(cuid()) |
| `profileId` | String | @map("profile_id") |
| `templateCardId` | String? | @map("template_card_id") |
| `nickname` | String? | |
| `annualFee` | Float | @map("annual_fee") |
| `lastFour` | String? | @map("last_four") |
| `openDateUtc` | String? | @map("open_date_utc") |
| `closeDateUtc` | String? | @map("close_date_utc") |
| `statementCutUtc` | String? | @map("statement_cut_utc") |
| `welcomeOfferProgress` | String? | @map("welcome_offer_progress") |
| `status` | CardStaus | @default(Active) |
| `notes` | String? | |
| `subSpending` | Float? | @map("sub_spending") |
| `subDuration` | Int? | @map("sub_duration") |
| `subDurationUnit` | CardSubDurationUnit? | @map("sub_duration_unit") |
| `benefitsLink` | ProfileCardBenefit[] | |
| `offers` | Offer[] | |
| `applications` | Application[] | |
| `profile` | Profile | @relation(fields: [profileId], references: [id], onDelete: Cascade) |
| `template` | Card? | @relation(fields: [templateCardId], references: [id]) |

### ProfileCardBenefit
| Field | Type | Description |
| :--- | :--- | :--- |
| `profileCardId` | String | @map("profile_card_id") |
| `benefitId` | String | @map("benefit_id") |
| `profileCard` | ProfileCard | @relation(fields: [profileCardId], references: [id], onDelete: Cascade) |
| `benefit` | Benefit | @relation(fields: [benefitId], references: [id], onDelete: Cascade) |

### Transaction
| Field | Type | Description |
| :--- | :--- | :--- |
| `id` | String | @id @default(cuid()) @map("_id") |
| `benefitId` | String? | @map("benefit_id") |
| `notes` | String? | |
| `dateUtc` | String? | |
| `amount` | Float | |
| `benefit` | Benefit? | @relation(fields: [benefitId], references: [id]) |
