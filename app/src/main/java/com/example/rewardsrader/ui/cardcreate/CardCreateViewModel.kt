package com.example.rewardsrader.ui.cardcreate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rewardsrader.data.local.entity.BenefitEntity
import com.example.rewardsrader.data.local.entity.BenefitType
import com.example.rewardsrader.data.local.entity.TemplateCardWithBenefits
import com.example.rewardsrader.data.local.repository.CardTemplateSource
import com.example.rewardsrader.template.CardTemplateImporterContract
import com.example.rewardsrader.template.ImportResult
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CardCreateViewModel(
    private val cardTemplates: CardTemplateSource,
    private val importer: CardTemplateImporterContract
) : ViewModel() {

    private val _state = MutableStateFlow(CardCreateState())
    val state: StateFlow<CardCreateState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<CardCreateEvent>()
    val events: SharedFlow<CardCreateEvent> = _events.asSharedFlow()

    private val dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US)
    private var cachedResults: List<CardSearchItem> = emptyList()

    fun loadTemplates() {
        if (!_state.value.isLoading && cachedResults.isNotEmpty()) {
            return
        }
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            runCatching {
                val issuers = cardTemplates.getIssuers()
                val cards = cardTemplates.getCards()
                val templates = cardTemplates.getTemplateCardsWithBenefits()
                val faceUrls = cards.associate { it.id to cardTemplates.getPreferredCardFaceUrl(it.id) }
                Triple(issuers, cards, Pair(templates, faceUrls))
            }.onSuccess { (issuers, cards, templateAndFaces) ->
                val (templates, faceUrls) = templateAndFaces
                val issuerOptions = issuers.map { IssuerOption(id = it.id, name = it.name) }
                val searchItems = buildSearchItems(issuerOptions, cards, templates, faceUrls)
                cachedResults = searchItems
                val networks = searchItems.map { it.network }.distinct().sorted()
                val segments = searchItems.map { it.segment }.distinct().sorted()
                val instruments = searchItems.map { it.paymentInstrument }.distinct().sorted()
                val categories = searchItems.flatMap { it.benefitCategories }.distinct().sorted()
                val feeBounds = buildFeeBounds(searchItems)
                val initialFilters = _state.value.filters.copy(
                    annualFeeRange = feeBounds
                )
                val nextState = CardCreateState(
                    isLoading = false,
                    query = "",
                    sortMode = SortMode.PRODUCT,
                    filters = initialFilters,
                    issuers = issuerOptions,
                    networks = networks,
                    segments = segments,
                    paymentInstruments = instruments,
                    benefitCategories = categories,
                    results = searchItems,
                    feeRangeBounds = feeBounds,
                    filteredResults = applyFilters(searchItems, "", SortMode.PRODUCT, initialFilters)
                )
                _state.value = nextState
            }.onFailure {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = it.message ?: "Failed to load card templates"
                )
            }
        }
    }

    fun updateQuery(query: String) {
        _state.update { current ->
            val trimmed = query.trimStart()
            val filtered = applyFilters(
                results = cachedResults,
                query = trimmed,
                sortMode = current.sortMode,
                filters = current.filters
            )
            current.copy(query = trimmed, filteredResults = filtered, error = null)
        }
    }

    fun setSortMode(mode: SortMode) {
        _state.update { current ->
            val filtered = applyFilters(
                results = cachedResults,
                query = current.query,
                sortMode = mode,
                filters = current.filters
            )
            current.copy(sortMode = mode, filteredResults = filtered)
        }
    }

    fun toggleIssuerFilter(issuerId: String) {
        updateFilters { filters ->
            val next = filters.issuerIds.toggle(issuerId)
            filters.copy(issuerIds = next)
        }
    }

    fun toggleNetworkFilter(network: String) {
        updateFilters { filters ->
            val next = filters.networks.toggle(network)
            filters.copy(networks = next)
        }
    }

    fun toggleSegmentFilter(segment: String) {
        updateFilters { filters ->
            val next = filters.segments.toggle(segment)
            filters.copy(segments = next)
        }
    }

    fun togglePaymentInstrument(instrument: String) {
        updateFilters { filters ->
            val next = filters.paymentInstruments.toggle(instrument)
            filters.copy(paymentInstruments = next)
        }
    }

    fun toggleBenefitType(type: String) {
        updateFilters { filters ->
            val next = filters.benefitTypes.toggle(type)
            filters.copy(benefitTypes = next)
        }
    }

    fun toggleBenefitCategory(category: String) {
        updateFilters { filters ->
            val next = filters.benefitCategories.toggle(category)
            filters.copy(benefitCategories = next)
        }
    }

    fun updateFeeRange(range: ClosedFloatingPointRange<Float>) {
        updateFilters { filters ->
            filters.copy(annualFeeRange = range)
        }
    }

    fun toggleNoAnnualFeeOnly() {
        updateFilters { filters ->
            filters.copy(noAnnualFeeOnly = !filters.noAnnualFeeOnly)
        }
    }

    fun resetFilters() {
        _state.update { current ->
            val resetFilters = CardCreateFilters(
                annualFeeRange = current.feeRangeBounds
            )
            val filtered = applyFilters(
                results = cachedResults,
                query = current.query,
                sortMode = current.sortMode,
                filters = resetFilters
            )
            current.copy(filters = resetFilters, filteredResults = filtered)
        }
    }

    fun createCard(cardId: String) {
        if (_state.value.isSaving) return
        _state.update { it.copy(isSaving = true, error = null) }
        val openDateUtc = LocalDate.now(ZoneOffset.UTC).format(dateFormatter)
        viewModelScope.launch {
            when (
                val result = importer.importFromDatabase(
                    cardId = cardId,
                    openDateUtc = openDateUtc,
                    statementCutUtc = null,
                    applicationStatus = "pending",
                    welcomeOfferProgress = null
                )
            ) {
                is ImportResult.Success -> {
                    _state.update { it.copy(isSaving = false) }
                    _events.emit(CardCreateEvent.Created)
                }
                is ImportResult.Failure -> {
                    _state.update { it.copy(isSaving = false, error = result.reason) }
                    _events.emit(CardCreateEvent.Error(result.reason))
                }
            }
        }
    }

    private fun updateFilters(transform: (CardCreateFilters) -> CardCreateFilters) {
        _state.update { current ->
            val nextFilters = transform(current.filters)
            val filtered = applyFilters(
                results = cachedResults,
                query = current.query,
                sortMode = current.sortMode,
                filters = nextFilters
            )
            current.copy(filters = nextFilters, filteredResults = filtered)
        }
    }

    private fun buildSearchItems(
        issuerOptions: List<IssuerOption>,
        cards: List<com.example.rewardsrader.data.local.entity.CardEntity>,
        templates: List<TemplateCardWithBenefits>,
        faceUrls: Map<String, String?>
    ): List<CardSearchItem> {
        val issuersById = issuerOptions.associateBy { it.id }
        val benefitsByCardId = templates.associateBy { it.card.id }

        return cards.map { card ->
            val template = benefitsByCardId[card.id]
            val benefitTypes = template?.benefits?.mapTo(mutableSetOf()) { it.type.toDisplayName() } ?: emptySet()
            val benefitCategories = template?.benefits?.flatMapTo(mutableSetOf()) { it.category.map { cat -> cat.name } }
                ?: emptySet()
            CardSearchItem(
                id = card.id,
                issuerId = card.issuerId,
                issuerName = issuersById[card.issuerId]?.name ?: card.issuerId,
                productName = card.productName,
                cardFaceUrl = faceUrls[card.id],
                network = card.network.name,
                segment = card.segment.name,
                paymentInstrument = card.paymentInstrument.name,
                annualFee = card.annualFee,
                benefitTypes = benefitTypes,
                benefitCategories = benefitCategories
            )
        }
    }

    private fun applyFilters(
        results: List<CardSearchItem>,
        query: String,
        sortMode: SortMode,
        filters: CardCreateFilters
    ): List<CardSearchItem> {
        var filtered = results
        if (query.isNotBlank()) {
            val q = query.lowercase(Locale.US)
            filtered = filtered.filter {
                it.productName.lowercase(Locale.US).contains(q) ||
                    it.issuerName.lowercase(Locale.US).contains(q)
            }
        }
        if (filters.issuerIds.isNotEmpty()) {
            filtered = filtered.filter { filters.issuerIds.contains(it.issuerId) }
        }
        if (filters.networks.isNotEmpty()) {
            filtered = filtered.filter { filters.networks.contains(it.network) }
        }
        if (filters.segments.isNotEmpty()) {
            filtered = filtered.filter { filters.segments.contains(it.segment) }
        }
        if (filters.paymentInstruments.isNotEmpty()) {
            filtered = filtered.filter { filters.paymentInstruments.contains(it.paymentInstrument) }
        }
        if (filters.benefitTypes.isNotEmpty()) {
            filtered = filtered.filter { result ->
                result.benefitTypes.any { filters.benefitTypes.contains(it) }
            }
        }
        if (filters.benefitCategories.isNotEmpty()) {
            filtered = filtered.filter { result ->
                result.benefitCategories.any { filters.benefitCategories.contains(it) }
            }
        }
        filters.annualFeeRange?.let { range ->
            filtered = filtered.filter { it.annualFee.toFloat() in range }
        }
        if (filters.noAnnualFeeOnly) {
            filtered = filtered.filter { it.annualFee <= 0.0 }
        }

        val comparator = when (sortMode) {
            SortMode.PRODUCT -> compareBy<CardSearchItem> { it.productName.lowercase(Locale.US) }
            SortMode.ISSUER_PRODUCT -> compareBy<CardSearchItem> { it.issuerName.lowercase(Locale.US) }
                .thenBy { it.productName.lowercase(Locale.US) }
            SortMode.ANNUAL_FEE_LOW_HIGH -> compareBy<CardSearchItem> { it.annualFee }
            SortMode.ANNUAL_FEE_HIGH_LOW -> compareByDescending<CardSearchItem> { it.annualFee }
            SortMode.NETWORK -> compareBy<CardSearchItem> { it.network }.thenBy { it.productName.lowercase(Locale.US) }
        }
        return filtered.sortedWith(comparator)
    }

    private fun buildFeeBounds(items: List<CardSearchItem>): ClosedFloatingPointRange<Float> {
        if (items.isEmpty()) return 0f..0f
        val min = items.minOf { it.annualFee.toFloat() }
        val max = items.maxOf { it.annualFee.toFloat() }
        return min..max
    }

    private fun BenefitType.toDisplayName(): String = when (this) {
        BenefitType.Credit -> "Credit"
        BenefitType.Multiplier -> "Multiplier"
    }

    private fun <T> Set<T>.toggle(value: T): Set<T> =
        if (contains(value)) this - value else this + value

    companion object {
        fun factory(repository: CardTemplateSource, importer: CardTemplateImporterContract): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return CardCreateViewModel(repository, importer) as T
                }
            }
        }
    }
}

sealed class CardCreateEvent {
    object Created : CardCreateEvent()
    data class Error(val message: String) : CardCreateEvent()
}
