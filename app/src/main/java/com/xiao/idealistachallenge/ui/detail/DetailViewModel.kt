package com.xiao.idealistachallenge.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xiao.idealistachallenge.core.ErrorMessageMapper
import com.xiao.idealistachallenge.core.UserFacingError
import com.xiao.idealistachallenge.data.repository.AdRepository
import com.xiao.idealistachallenge.data.repository.FavoriteRepository
import com.xiao.idealistachallenge.model.PropertyDetails
import java.util.concurrent.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DetailUiState {
    data object Loading : DetailUiState

    data class Content(
        val details: PropertyDetails,
        val favoritedAtEpochMillis: Long?,
        val isDescriptionExpanded: Boolean = false,
    ) : DetailUiState

    data class Error(
        val userFacingError: UserFacingError,
    ) : DetailUiState
}

class DetailViewModel(
    private val adRepository: AdRepository,
    private val favoriteRepository: FavoriteRepository,
    private val selectedAdId: String,
    private val dispatcher: CoroutineDispatcher,
    private val nowEpochMillis: () -> Long = System::currentTimeMillis,
) : ViewModel() {
    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var favoriteObservationJob: Job? = null
    private var favoriteActionJob: Job? = null
    private var hasLoaded = false
    private var isDescriptionExpanded = false

    fun load() {
        if (hasLoaded || loadJob?.isActive == true) return
        hasLoaded = true
        loadJob = viewModelScope.launch(dispatcher) {
            _uiState.value = DetailUiState.Loading
            try {
                adRepository.loadDetails(selectedAdId).fold(
                    onSuccess = { details ->
                        observeFavorite(details)
                    },
                    onFailure = { failure ->
                        _uiState.value = DetailUiState.Error(ErrorMessageMapper.forDetail(failure))
                    },
                )
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: Exception) {
                _uiState.value = DetailUiState.Error(ErrorMessageMapper.forDetail(failure))
            }
        }
    }

    fun retry() {
        if (loadJob?.isActive == true) return
        hasLoaded = false
        isDescriptionExpanded = false
        load()
    }

    fun toggleDescriptionExpansion() {
        val content = _uiState.value as? DetailUiState.Content ?: return
        if (content.details.description.isNullOrBlank()) return
        isDescriptionExpanded = !isDescriptionExpanded
        _uiState.value = content.copy(isDescriptionExpanded = isDescriptionExpanded)
    }

    fun toggleFavorite() {
        if (favoriteActionJob?.isActive == true) return
        val content = _uiState.value as? DetailUiState.Content ?: return

        favoriteActionJob = viewModelScope.launch(dispatcher) {
            if (content.favoritedAtEpochMillis == null) {
                favoriteRepository.favorite(selectedAdId, nowEpochMillis())
            } else {
                favoriteRepository.unfavorite(selectedAdId)
            }
        }
    }

    private fun observeFavorite(details: PropertyDetails) {
        favoriteObservationJob?.cancel()
        favoriteObservationJob = viewModelScope.launch(dispatcher) {
            favoriteRepository.observeFavorite(selectedAdId).collect { favorite ->
                _uiState.value = DetailUiState.Content(
                    details = details,
                    favoritedAtEpochMillis = favorite?.favoritedAtEpochMillis,
                    isDescriptionExpanded = isDescriptionExpanded,
                )
            }
        }
    }
}
