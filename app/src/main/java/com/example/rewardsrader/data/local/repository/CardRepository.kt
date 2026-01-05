package com.example.rewardsrader.data.local.repository

import com.example.rewardsrader.data.local.dao.ApplicationDao
import com.example.rewardsrader.data.local.dao.BenefitDao
import com.example.rewardsrader.data.local.dao.CardBenefitDao
import com.example.rewardsrader.data.local.dao.CardDao
import com.example.rewardsrader.data.local.dao.CardFaceDao
import com.example.rewardsrader.data.local.dao.IssuerDao
import com.example.rewardsrader.data.local.dao.NotificationRuleDao
import com.example.rewardsrader.data.local.dao.OfferDao
import com.example.rewardsrader.data.local.dao.ProfileCardBenefitDao
import com.example.rewardsrader.data.local.dao.ProfileCardDao
import com.example.rewardsrader.data.local.dao.ProfileDao
import com.example.rewardsrader.data.local.dao.TransactionDao
import com.example.rewardsrader.data.local.entity.ApplicationEntity
import com.example.rewardsrader.data.local.entity.BenefitEntity
import com.example.rewardsrader.data.local.entity.CardBenefitEntity
import com.example.rewardsrader.data.local.entity.CardEntity
import com.example.rewardsrader.data.local.entity.CardFaceEntity
import com.example.rewardsrader.data.local.entity.IssuerEntity
import com.example.rewardsrader.data.local.entity.NotificationRuleEntity
import com.example.rewardsrader.data.local.entity.OfferEntity
import com.example.rewardsrader.data.local.entity.ProfileCardBenefitEntity
import com.example.rewardsrader.data.local.entity.ProfileCardEntity
import com.example.rewardsrader.data.local.entity.ProfileEntity
import com.example.rewardsrader.data.local.entity.ProfileCardWithRelations
import com.example.rewardsrader.data.local.entity.TransactionEntity
import java.util.UUID

/**
 * Repository facade around the Prisma-aligned Room schema.
 * This preserves only simple upsert/query helpers; UI flows will be refactored later.
 */
class CardRepository(
    private val issuerDao: IssuerDao,
    private val cardDao: CardDao,
    private val cardFaceDao: CardFaceDao,
    private val cardBenefitDao: CardBenefitDao,
    private val profileDao: ProfileDao,
    private val profileCardDao: ProfileCardDao,
    private val profileCardBenefitDao: ProfileCardBenefitDao,
    private val benefitDao: BenefitDao,
    private val transactionDao: TransactionDao,
    private val notificationRuleDao: NotificationRuleDao,
    private val offerDao: OfferDao,
    private val applicationDao: ApplicationDao
) {
    suspend fun upsertIssuers(issuers: List<IssuerEntity>) {
        if (issuers.isNotEmpty()) issuerDao.insertAll(issuers)
    }

    suspend fun upsertCards(cards: List<CardEntity>) {
        if (cards.isNotEmpty()) cards.forEach { cardDao.insert(it) }
    }

    suspend fun upsertCardFaces(faces: List<CardFaceEntity>) {
        if (faces.isNotEmpty()) cardFaceDao.insertAll(faces)
    }

    suspend fun upsertBenefits(benefits: List<BenefitEntity>) {
        if (benefits.isNotEmpty()) benefitDao.insertAll(benefits)
    }

    suspend fun upsertBenefit(benefit: BenefitEntity) {
        benefitDao.insert(benefit)
    }

    suspend fun getBenefit(benefitId: String): BenefitEntity? = benefitDao.getById(benefitId)

    suspend fun upsertCardBenefits(links: List<CardBenefitEntity>) {
        if (links.isNotEmpty()) cardBenefitDao.insertAll(links)
    }

    suspend fun upsertProfiles(profiles: List<ProfileEntity>) {
        if (profiles.isNotEmpty()) profileDao.insertAll(profiles)
    }

    suspend fun ensureProfile(profileId: String, name: String): ProfileEntity {
        val existing = profileDao.getById(profileId)
        if (existing != null) return existing
        val profile = ProfileEntity(id = profileId, name = name)
        profileDao.insert(profile)
        return profile
    }

    suspend fun upsertProfileCards(profileCards: List<ProfileCardEntity>) {
        if (profileCards.isNotEmpty()) profileCardDao.insertAll(profileCards)
    }

    suspend fun upsertProfileCardBenefits(links: List<ProfileCardBenefitEntity>) {
        if (links.isNotEmpty()) profileCardBenefitDao.insertAll(links)
    }

    suspend fun addTransactions(transactions: List<TransactionEntity>) {
        if (transactions.isNotEmpty()) transactionDao.insertAll(transactions)
    }

    suspend fun addNotificationRules(rules: List<NotificationRuleEntity>) {
        if (rules.isNotEmpty()) notificationRuleDao.insertAll(rules)
    }

    suspend fun addApplications(applications: List<ApplicationEntity>) {
        if (applications.isNotEmpty()) applicationDao.insertAll(applications)
    }

    suspend fun getProfileCards(profileId: String): List<ProfileCardEntity> =
        profileCardDao.getForProfile(profileId)

    suspend fun getProfileCardsWithRelations(profileId: String): List<ProfileCardWithRelations> =
        profileCardDao.getWithRelationsForProfile(profileId)

    suspend fun getProfileCardWithRelations(profileCardId: String): ProfileCardWithRelations? =
        profileCardDao.getWithRelations(profileCardId)

    suspend fun getBenefitsForProfileCard(profileCardId: String): List<BenefitEntity> {
        val links = profileCardBenefitDao.getForProfileCard(profileCardId)
        if (links.isEmpty()) return emptyList()
        return benefitDao.getByIds(links.map { it.benefitId })
    }

    suspend fun getOffersForProfileCard(profileCardId: String): List<OfferEntity> =
        offerDao.getForProfileCard(profileCardId)

    suspend fun getOffer(offerId: String): OfferEntity? = offerDao.getById(offerId)

    suspend fun addOffer(offer: OfferEntity) = offerDao.insert(offer)

    suspend fun updateOffer(offer: OfferEntity) = offerDao.update(offer)

    suspend fun deleteOffer(offerId: String) = offerDao.deleteById(offerId)

    suspend fun getTransactionsForBenefit(benefitId: String): List<TransactionEntity> =
        transactionDao.getForBenefit(benefitId)

    suspend fun getTransactionsForProfileCardBenefit(profileCardBenefitId: String): List<TransactionEntity> =
        transactionDao.getForProfileCardBenefit(profileCardBenefitId)

    suspend fun getBenefitsByIds(ids: List<String>): List<BenefitEntity> =
        benefitDao.getByIds(ids)

    suspend fun addBenefitForProfileCard(profileCardId: String, benefit: BenefitEntity): BenefitEntity {
        val benefitId = benefit.id.ifBlank { newId() }
        val finalBenefit = benefit.copy(id = benefitId)
        benefitDao.insert(finalBenefit)
        val link = ProfileCardBenefitEntity(
            id = newId(),
            profileCardId = profileCardId,
            benefitId = benefitId
        )
        profileCardBenefitDao.insert(link)
        return finalBenefit
    }

    suspend fun deleteProfileCard(profileCardId: String) {
        profileCardDao.deleteById(profileCardId)
    }

    suspend fun deleteBenefit(benefitId: String) {
        benefitDao.deleteById(benefitId)
    }

    suspend fun updateProfileCard(profileCardEntity: ProfileCardEntity) {
        profileCardDao.update(profileCardEntity)
    }

    fun newId(): String = UUID.randomUUID().toString()
}
