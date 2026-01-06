package com.example.rewardsrader.data.remote

import com.example.rewardsrader.data.local.entity.CardEntity
import com.example.rewardsrader.data.local.entity.CardNetwork
import com.example.rewardsrader.data.local.entity.IssuerEntity
import com.example.rewardsrader.data.local.repository.CardRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreSyncer(
    private val firestore: FirebaseFirestore,
    private val repository: CardRepository
    ) {

    suspend fun syncIssuersAndCards(): SyncResult {
        val issuers = firestore.collection("issuers")
            .get()
            .await()
            .documents
            .mapNotNull { it.toIssuerEntity() }
        repository.upsertIssuers(issuers)

        val cards = firestore.collection("cards")
            .get()
            .await()
            .documents
            .mapNotNull { it.toCardEntity() }
        repository.upsertCards(cards)

        return SyncResult(issuersSynced = issuers.size, cardsSynced = cards.size)
    }

    private fun DocumentSnapshot.toIssuerEntity(): IssuerEntity? {
        val name = stringField("name") ?: return null
        return IssuerEntity(id = id, name = name)
    }

    private fun DocumentSnapshot.toCardEntity(): CardEntity? {
        val issuerId = stringField("issuerId", "issuer_id") ?: return null
        val productName = stringField("productName", "product_name") ?: return null
        val networkValue = stringField("network")?.uppercase()
        val network = networkValue
            ?.let { runCatching { CardNetwork.valueOf(it) }.getOrNull() }
            ?: CardNetwork.Visa
        val annualFee = doubleField("annualFee", "annual_fee") ?: 0.0
        val foreignFee = doubleField("foreignFeeTransactionFee", "foreign_fee_transaction_fee") ?: 0.0
        return CardEntity(
            id = id,
            issuerId = issuerId,
            productName = productName,
            network = network,
            annualFee = annualFee,
            foreignFeeTransactionFee = foreignFee
        )
    }

    private fun DocumentSnapshot.stringField(primary: String, alternate: String? = null): String? {
        val primaryValue = getString(primary)
        if (!primaryValue.isNullOrBlank()) return primaryValue
        if (alternate != null) {
            val altValue = getString(alternate)
            if (!altValue.isNullOrBlank()) return altValue
        }
        return null
    }

    private fun DocumentSnapshot.doubleField(primary: String, alternate: String? = null): Double? {
        val primaryValue = getDouble(primary)
        if (primaryValue != null) return primaryValue
        if (alternate != null) {
            val altValue = getDouble(alternate)
            if (altValue != null) return altValue
        }
        val numberValue = get(primary)
        if (numberValue is Number) return numberValue.toDouble()
        if (alternate != null) {
            val altNumber = get(alternate)
            if (altNumber is Number) return altNumber.toDouble()
        }
        return null
    }

    data class SyncResult(
        val issuersSynced: Int,
        val cardsSynced: Int
    )
}
