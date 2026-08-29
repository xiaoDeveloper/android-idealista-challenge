package com.xiao.idealistachallenge.ui.listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xiao.idealistachallenge.core.ErrorMessageMapper
import com.xiao.idealistachallenge.core.UserFacingError
import com.xiao.idealistachallenge.data.repository.AdRepository
import com.xiao.idealistachallenge.data.repository.FavoriteRepository
import com.xiao.idealistachallenge.model.PropertyAd
import java.util.concurrent.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ListingCategory {
    ALL,
    SALE,
    RENT,
}

enum class PriceSortDirection {
    ASCENDING,
    DESCENDING,
}

data class ListingDiscoveryUiState(
    val category: ListingCategory = ListingCategory.ALL,
    val priceSortDirection: PriceSortDirection? = null,
    val favoritesOnly: Boolean = false,
)

data class ListingRowUiModel(
    val ad: PropertyAd,
    val favoritedAtEpochMillis: Long?,
)

sealed interface ListingUiState {
    data object Loading : ListingUiState

    data class Content(
        val rows: List<ListingRowUiModel>,
        val discovery: ListingDiscoveryUiState = ListingDiscoveryUiState(),
        val isFilteredEmpty: Boolean = false,
    ) : ListingUiState

    data object Empty : ListingUiState

    data class Error(
        val userFacingError: UserFacingError,
    ) : ListingUiState
}

class ListingViewModel(
    private val adRepository: AdRepository,
    private val favoriteRepository: FavoriteRepository,
    private val dispatcher: CoroutineDispatcher,
    private val nowEpochMillis: () -> Long = System::currentTimeMillis,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ListingUiState>(ListingUiState.Loading)
    val uiState: StateFlow<ListingUiState> = _uiState.asStateFlow()

    private val _discoveryState = MutableStateFlow(ListingDiscoveryUiState())
    val discoveryState: StateFlow<ListingDiscoveryUiState> = _discoveryState.asStateFlow()

    private var loadJob: Job? = null
    private var favoriteObservationJob: Job? = null
    private val favoriteActionJobs = mutableMapOf<String, Job>()
    private var hasLoaded = false

    fun load() {
        if (hasLoaded || loadJob?.isActive == true) return

        hasLoaded = true
        loadJob = viewModelScope.launch(dispatcher) {
            _uiState.value = ListingUiState.Loading

            try {
                adRepository.loadAds().fold(
                    onSuccess = { ads ->
                        if (ads.isEmpty()) {
                            _uiState.value = ListingUiState.Empty
                        } else {
                            observeFavoriteRows(ads)
                        }
                    },
                    onFailure = { failure ->
                        _uiState.value = ListingUiState.Error(
                            userFacingError = ErrorMessageMapper.forListing(failure),
                        )
                    },
                )
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: Exception) {
                _uiState.value = ListingUiState.Error(
                    userFacingError = ErrorMessageMapper.forListing(failure),
                )
            }
        }
    }

    fun retry() {
        if (loadJob?.isActive == true) return
        hasLoaded = false
        load()
    }

    fun selectCategory(category: ListingCategory) {
        _discoveryState.update { current ->
            if (category == ListingCategory.ALL) {
                current.copy(category = ListingCategory.ALL, priceSortDirection = null)
            } else {
                current.copy(category = category)
            }
        }
    }

    fun selectPriceSortDirection(direction: PriceSortDirection) {
        _discoveryState.update { current ->
            if (current.category == ListingCategory.ALL) {
                current
            } else {
                current.copy(priceSortDirection = direction)
            }
        }
    }

    fun toggleFavoritesOnly(favoritesOnly: Boolean = !_discoveryState.value.favoritesOnly) {
        _discoveryState.update { current ->
            current.copy(favoritesOnly = favoritesOnly)
        }
    }

    fun resetDiscovery() {
        _discoveryState.value = ListingDiscoveryUiState()
    }

    fun toggleFavorite(adId: String, favoritedAtEpochMillis: Long?) {
        if (favoriteActionJobs[adId]?.isActive == true) return

        favoriteActionJobs[adId] = viewModelScope.launch(dispatcher) {
            try {
                if (favoritedAtEpochMillis == null) {
                    favoriteRepository.favorite(adId, nowEpochMillis())
                } else {
                    favoriteRepository.unfavorite(adId)
                }
            } finally {
                favoriteActionJobs.remove(adId)
            }
        }
    }

    private fun observeFavoriteRows(ads: List<PropertyAd>) {
        favoriteObservationJob?.cancel()
        favoriteObservationJob = viewModelScope.launch(dispatcher) {
            val favoriteFlows = if (ads.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(ads.map { ad -> favoriteRepository.observeFavorite(ad.propertyCode) }) { it.toList() }
            }

            combine(favoriteFlows, _discoveryState) { favorites, discovery ->
                val allRows = ads.mapIndexed { index, ad ->
                    ListingRowUiModel(
                        ad = ad,
                        favoritedAtEpochMillis = favorites[index]?.favoritedAtEpochMillis,
                    )
                }
                val filteredAndSortedRows = applyDiscovery(allRows, discovery)
                ListingUiState.Content(
                    rows = filteredAndSortedRows,
                    discovery = discovery,
                    isFilteredEmpty = allRows.isNotEmpty() && filteredAndSortedRows.isEmpty(),
                )
            }.collect { contentState ->
                _uiState.value = contentState
            }
        }
    }

    private fun applyDiscovery(
        rows: List<ListingRowUiModel>,
        discovery: ListingDiscoveryUiState,
    ): List<ListingRowUiModel> {
        val categoryFiltered = when (discovery.category) {
            ListingCategory.ALL -> rows
            ListingCategory.SALE -> rows.filter {
                it.ad.operation?.trim()?.equals("sale", ignoreCase = true) == true
            }
            ListingCategory.RENT -> rows.filter {
                it.ad.operation?.trim()?.equals("rent", ignoreCase = true) == true
            }
        }

        val favoritesFiltered = if (discovery.favoritesOnly) {
            categoryFiltered.filter { it.favoritedAtEpochMillis != null }
        } else {
            categoryFiltered
        }

        return when {
            discovery.category == ListingCategory.ALL || discovery.priceSortDirection == null -> favoritesFiltered
            discovery.priceSortDirection == PriceSortDirection.ASCENDING -> favoritesFiltered.sortedBy { it.ad.price }
            discovery.priceSortDirection == PriceSortDirection.DESCENDING -> favoritesFiltered.sortedByDescending { it.ad.price }
            else -> favoritesFiltered
        }
    }
}
