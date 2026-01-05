package com.example.rewardsrader.data.local

import androidx.room.TypeConverter
import com.example.rewardsrader.data.local.entity.BenefitCategory
import com.example.rewardsrader.data.local.entity.BenefitFrequency
import com.example.rewardsrader.data.local.entity.BenefitType
import com.example.rewardsrader.data.local.entity.CardNetwork
import com.example.rewardsrader.data.local.entity.CardStatus
import com.example.rewardsrader.data.local.entity.CardSubDurationUnit

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
}
