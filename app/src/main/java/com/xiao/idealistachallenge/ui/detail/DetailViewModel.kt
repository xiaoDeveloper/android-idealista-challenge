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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface DetailUiState {
    data object Loading : DetailUiState

    data class Content(
        val details: PropertyDetails,
        val favoritedAtEpochMillis: Long?,
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
) : ViewModel() {
    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var hasLoaded = false

    fun load() {
        if (hasLoaded || loadJob?.isActive == true) return
        hasLoaded = true
        loadJob = viewModelScope.launch(dispatcher) {
            _uiState.value = DetailUiState.Loading
            try {
                adRepository.loadDetails(selectedAdId).fold(
                    onSuccess = { details ->
                        val favorite = favoriteRepository.observeFavorite(selectedAdId).first()
                        _uiState.value = DetailUiState.Content(
                            details = details,
                            favoritedAtEpochMillis = favorite?.favoritedAtEpochMillis,
                        )
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
        load()
    }
}
