package com.talha.depremtakip.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talha.depremtakip.domain.model.Earthquake
import com.talha.depremtakip.domain.model.EarthquakeFilter
import com.talha.depremtakip.domain.model.SortOrder
import com.talha.depremtakip.domain.model.TimeRange
import com.talha.depremtakip.domain.time.Clock
import com.talha.depremtakip.domain.usecase.FilterEarthquakesUseCase
import com.talha.depremtakip.domain.usecase.GetEarthquakesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class ListUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val earthquakes: List<Earthquake> = emptyList(),
    val totalCount: Int = 0,
    val range: TimeRange = TimeRange.LAST_24_HOURS,
    val filter: EarthquakeFilter = EarthquakeFilter(),
    val isOffline: Boolean = false,
    val lastUpdated: Instant? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ListViewModel @Inject constructor(
    private val getEarthquakes: GetEarthquakesUseCase,
    private val filterEarthquakes: FilterEarthquakesUseCase,
    private val clock: Clock
) : ViewModel() {

    private val rangeFlow = MutableStateFlow(TimeRange.LAST_24_HOURS)
    private val _state = MutableStateFlow(ListUiState())
    val state: StateFlow<ListUiState> = _state.asStateFlow()

    private var latest: List<Earthquake> = emptyList()

    init {
        viewModelScope.launch {
            rangeFlow.flatMapLatest { range -> getEarthquakes(range) }.collect { all ->
                latest = all
                val current = _state.value
                _state.update {
                    it.copy(
                        earthquakes = filterEarthquakes(all, current.filter),
                        totalCount = all.size,
                        isLoading = false
                    )
                }
            }
        }
        refresh()
    }

    fun refresh() {
        val currentRange = rangeFlow.value
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = !it.isLoading) }
            val result = getEarthquakes.refresh(currentRange)
            _state.update { current ->
                result.fold(
                    onSuccess = {
                        current.copy(
                            isLoading = false,
                            isRefreshing = false,
                            isOffline = false,
                            lastUpdated = clock.now()
                        )
                    },
                    onFailure = {
                        current.copy(
                            isLoading = false,
                            isRefreshing = false,
                            isOffline = true
                        )
                    }
                )
            }
        }
    }

    fun setRange(range: TimeRange) {
        if (range == rangeFlow.value) return
        rangeFlow.value = range
        _state.update { it.copy(range = range, isLoading = true) }
        refresh()
    }

    fun setMinMagnitude(value: Double) {
        applyFilter(_state.value.filter.copy(minMagnitude = value))
    }

    fun setQuery(query: String) {
        applyFilter(_state.value.filter.copy(query = query))
    }

    fun setSortOrder(order: SortOrder) {
        applyFilter(_state.value.filter.copy(sortOrder = order))
    }

    private fun applyFilter(filter: EarthquakeFilter) {
        _state.update {
            it.copy(
                filter = filter,
                earthquakes = filterEarthquakes(latest, filter)
            )
        }
    }
}
