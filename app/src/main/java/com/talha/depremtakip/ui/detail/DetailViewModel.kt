package com.talha.depremtakip.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talha.depremtakip.domain.model.Earthquake
import com.talha.depremtakip.domain.usecase.GetEarthquakeDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val isLoading: Boolean = true,
    val earthquake: Earthquake? = null
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getEarthquakeDetail: GetEarthquakeDetailUseCase
) : ViewModel() {

    private val quakeId: String = checkNotNull(savedStateHandle["id"])

    private val _state = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getEarthquakeDetail(quakeId).collect { quake ->
                _state.value = DetailUiState(isLoading = false, earthquake = quake)
            }
        }
    }
}
