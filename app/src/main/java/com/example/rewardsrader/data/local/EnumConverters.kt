package com.example.rewardsrader.data.local

import androidx.room.TypeConverter
import com.example.rewardsrader.data.local.entity.BenefitCategory
import com.example.rewardsrader.data.local.entity.BenefitFrequency
import com.example.rewardsrader.data.local.entity.BenefitType
import com.example.rewardsrader.data.local.entity.CardNetwork
import com.example.rewardsrader.data.local.entity.CardSegment
import com.example.rewardsrader.data.local.entity.CardStatus
import com.example.rewardsrader.data.local.entity.CardSubDurationUnit
import com.example.rewardsrader.data.local.entity.PaymentInstrument
import com.example.rewardsrader.data.local.entity.TrackerSourceType

class EnumConverters {
    @TypeConverter
    fun fromCardNetwork(value: CardNetwork?): String? = value?.name

    @TypeConverter
    fun toCardNetwork(value: String?): CardNetwork? = value?.let { CardNetwork.valueOf(it) }

    @TypeConverter
    fun fromCardStatus(value: CardStatus?): String? = value?.name

    @TypeConverter
    fun toCardStatus(value: String?): CardStatus? = value?.let { CardStatus.valueOf(it) }

    @TypeConverter
    fun fromPaymentInstrument(value: PaymentInstrument?): String? = value?.name

    @TypeConverter
    fun toPaymentInstrument(value: String?): PaymentInstrument? =
        value?.let { PaymentInstrument.valueOf(it) }

    @TypeConverter
    fun fromCardSegment(value: CardSegment?): String? = value?.name

    @TypeConverter
    fun toCardSegment(value: String?): CardSegment? = value?.let { CardSegment.valueOf(it) }

    @TypeConverter
    fun fromCardSubDurationUnit(value: CardSubDurationUnit?): String? = value?.name

    @TypeConverter
    fun toCardSubDurationUnit(value: String?): CardSubDurationUnit? =
        value?.let { CardSubDurationUnit.valueOf(it) }

    @TypeConverter
    fun fromBenefitType(value: BenefitType?): String? = value?.name

    @TypeConverter
    fun toBenefitType(value: String?): BenefitType? = value?.let { BenefitType.valueOf(it) }

    @TypeConverter
    fun fromBenefitFrequency(value: BenefitFrequency?): String? = value?.name

    @TypeConverter
    fun toBenefitFrequency(value: String?): BenefitFrequency? =
        value?.let { BenefitFrequency.valueOf(it) }

    @TypeConverter
    fun fromBenefitCategories(categories: List<BenefitCategory>?): String? =
        categories?.joinToString(",") { it.name }

    @TypeConverter
    fun toBenefitCategories(value: String?): List<BenefitCategory> =
        value?.takeIf { it.isNotBlank() }
            ?.split(",")
            ?.mapNotNull { runCatching { BenefitCategory.valueOf(it) }.getOrNull() }
            ?: emptyList()

    @TypeConverter
    fun fromTrackerSourceType(value: TrackerSourceType?): String? = value?.name

    @TypeConverter
    fun toTrackerSourceType(value: String?): TrackerSourceType? =
        value?.let { TrackerSourceType.valueOf(it) }
}
