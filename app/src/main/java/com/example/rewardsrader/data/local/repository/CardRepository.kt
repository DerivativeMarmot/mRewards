package com.example.rewardsrader.data.local.repository

import com.example.rewardsrader.data.local.dao.ApplicationDao
import com.example.rewardsrader.data.local.dao.BenefitDao
import com.example.rewardsrader.data.local.dao.CardDao
import com.example.rewardsrader.data.local.dao.OfferDao
import com.example.rewardsrader.data.local.dao.NotificationRuleDao
import com.example.rewardsrader.data.local.dao.UsageEntryDao
import com.example.rewardsrader.data.local.entity.ApplicationEntity
import com.example.rewardsrader.data.local.entity.BenefitEntity
import com.example.rewardsrader.data.local.entity.CardEntity
import com.example.rewardsrader.data.local.entity.CardWithBenefits
import com.example.rewardsrader.data.local.entity.NotificationRuleEntity
import com.example.rewardsrader.data.local.entity.OfferEntity
import com.example.rewardsrader.data.local.entity.UsageEntryEntity

class CardRepository(
    private val cardDao: CardDao,
    private val benefitDao: BenefitDao,
    private val applicationDao: ApplicationDao,
    private val usageEntryDao: UsageEntryDao,
    private val notificationRuleDao: NotificationRuleDao,
    private val offerDao: OfferDao
) {
    suspend fun addCard(card: CardEntity, benefits: List<BenefitEntity>): Long {
        val cardId = cardDao.insert(card)
        val benefitsWithIds = benefits.map { it.copy(cardId = cardId) }
        benefitDao.insertAll(benefitsWithIds)
        return cardId
    }

    suspend fun addCardWithDetails(
        card: CardEntity,
        benefits: List<BenefitEntity>,
        application: ApplicationEntity?
    ): Long {
        val cardId = cardDao.insert(card)
        val benefitsWithIds = benefits.map { it.copy(cardId = cardId) }
        benefitDao.insertAll(benefitsWithIds)
        application?.let { app ->
            applicationDao.insert(app.copy(cardId = cardId))
        }
        return cardId
    }

    suspend fun getCards(): List<CardEntity> = cardDao.getAll()

    suspend fun getCardsWithBenefits(): List<CardWithBenefits> = cardDao.getAllWithBenefits()

    suspend fun getCardWithBenefits(cardId: Long): CardWithBenefits? = cardDao.getWithBenefits(cardId)

    suspend fun removeCard(cardId: Long) {
        cardDao.deleteById(cardId)
    }

    suspend fun getApplicationsForCard(cardId: Long) = applicationDao.getForCard(cardId)

    suspend fun getCard(cardId: Long): CardEntity? = cardDao.getById(cardId)

    suspend fun updateCard(card: CardEntity) {
        cardDao.update(card)
    }

    suspend fun getBenefitsForCard(cardId: Long): List<BenefitEntity> = benefitDao.getForCard(cardId)

    suspend fun addBenefit(benefit: BenefitEntity): Long = benefitDao.insert(benefit)

    suspend fun getBenefit(benefitId: Long): BenefitEntity? = benefitDao.getById(benefitId)

    suspend fun updateBenefit(benefit: BenefitEntity) {
        benefitDao.update(benefit)
    }

    suspend fun deleteBenefit(benefitId: Long) {
        benefitDao.deleteById(benefitId)
    }

    suspend fun getUsageForBenefits(benefitIds: List<Long>): List<UsageEntryEntity> =
        if (benefitIds.isEmpty()) emptyList() else usageEntryDao.getForBenefits(benefitIds)

    suspend fun getNotificationRulesForBenefits(benefitIds: List<Long>): List<NotificationRuleEntity> =
        if (benefitIds.isEmpty()) emptyList() else notificationRuleDao.getForBenefits(benefitIds)

    suspend fun insertUsageEntries(entries: List<UsageEntryEntity>) {
        if (entries.isNotEmpty()) usageEntryDao.insertAll(entries)
    }

    suspend fun insertNotificationRules(rules: List<NotificationRuleEntity>) {
        if (rules.isNotEmpty()) notificationRuleDao.insertAll(rules)
    }

    suspend fun insertApplications(applications: List<ApplicationEntity>) {
        if (applications.isNotEmpty()) applicationDao.insertAll(applications)
    }

    suspend fun getOffersForCard(cardId: Long): List<OfferEntity> = offerDao.getForCard(cardId)

    suspend fun addOffer(offer: OfferEntity): Long = offerDao.insert(offer)

    suspend fun getOffer(offerId: Long): OfferEntity? = offerDao.getById(offerId)

    suspend fun updateOffer(offer: OfferEntity) = offerDao.update(offer)

    suspend fun deleteOffer(offerId: Long) = offerDao.deleteById(offerId)
}
