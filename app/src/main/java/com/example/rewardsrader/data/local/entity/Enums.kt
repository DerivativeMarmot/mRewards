package com.example.rewardsrader.data.local.entity

enum class CardNetwork {
    Visa,
    Mastercard,
    Discover,
    Amex
}

enum class PaymentInstrument {
    Credit,
    Debit,
    Charge
}

enum class CardSegment {
    Personal,
    Business
}

enum class CardStatus {
    Active,
    Closed,
    Pending
}

enum class CardSubDurationUnit {
    Day,
    Month
}

enum class BenefitType {
    Credit,
    Multiplier
}

enum class BenefitFrequency {
    Monthly,
    Quarterly,
    SemiAnnually,
    Annually,
    EveryTransaction,
    EveryAnniversary
}

enum class BenefitCategory {
    Dining,
    OnlineShopping,
    Grocery,
    Restaurant,
    DrugStore,
    Travel,
    Gas,
    EVCharging,
    Streaming,
    Transit,
    Utilities,
    Others,
    RideShare,
    Supermarket,
    RetailStore
}
