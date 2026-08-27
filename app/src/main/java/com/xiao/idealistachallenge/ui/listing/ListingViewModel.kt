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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ListingRowUiModel(
    val ad: PropertyAd,
    val favoritedAtEpochMillis: Long?,
)

sealed interface ListingUiState {
    data object Loading : ListingUiState

    data class Content(
        val rows: List<ListingRowUiModel>,
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
) : ViewModel() {

    private val _uiState = MutableStateFlow<ListingUiState>(ListingUiState.Loading)
    val uiState: StateFlow<ListingUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var hasLoaded = false

    fun load() {
        if (hasLoaded || loadJob?.isActive == true) return

        hasLoaded = true
        loadJob = viewModelScope.launch(dispatcher) {
            _uiState.value = ListingUiState.Loading

            try {
                adRepository.loadAds().fold(
                    onSuccess = { ads ->
                        val rows = ads.map { ad ->
                            val favorite = favoriteRepository.observeFavorite(ad.propertyCode).first()
                            ListingRowUiModel(
                                ad = ad,
                                favoritedAtEpochMillis = favorite?.favoritedAtEpochMillis,
                            )
                        }
                        _uiState.value = if (rows.isEmpty()) {
                            ListingUiState.Empty
                        } else {
                            ListingUiState.Content(rows = rows.toList())
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
}
