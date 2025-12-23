package com.example.rewardsrader.data.local.repository

import com.example.rewardsrader.data.local.dao.BenefitDao
import com.example.rewardsrader.data.local.dao.CardDao
import com.example.rewardsrader.data.local.entity.BenefitEntity
import com.example.rewardsrader.data.local.entity.CardEntity
import com.example.rewardsrader.data.local.entity.CardWithBenefits

class CardRepository(
    private val cardDao: CardDao,
    private val benefitDao: BenefitDao
) {
    suspend fun addCard(card: CardEntity, benefits: List<BenefitEntity>): Long {
        val cardId = cardDao.insert(card)
        val benefitsWithIds = benefits.map { it.copy(cardId = cardId) }
        benefitDao.insertAll(benefitsWithIds)
        return cardId
    }

    suspend fun getCards(): List<CardEntity> = cardDao.getAll()

    suspend fun getCardsWithBenefits(): List<CardWithBenefits> = cardDao.getAllWithBenefits()

    suspend fun removeCard(cardId: Long) {
        cardDao.deleteById(cardId)
    }
}
