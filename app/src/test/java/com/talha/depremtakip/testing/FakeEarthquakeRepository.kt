package com.talha.depremtakip.testing

import com.talha.depremtakip.domain.model.Earthquake
import com.talha.depremtakip.domain.model.TimeRange
import com.talha.depremtakip.domain.repository.EarthquakeRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FakeEarthquakeRepository(
    initial: List<Earthquake> = emptyList(),
    private val hotFlow: MutableSharedFlow<List<Earthquake>>? = null
) : EarthquakeRepository {

    val storage = MutableStateFlow(initial)
    val cached: List<Earthquake>
        get() = storage.value

    var refreshResult: Result<Int> = Result.success(0)
    var refreshCalls: Int = 0
        private set
    var lastRange: TimeRange? = null
        private set
    var refreshError: Throwable? = null
    var refreshGate: CompletableDeferred<Unit>? = null

    override fun observeEarthquakes(): Flow<List<Earthquake>> =
        hotFlow ?: storage.asStateFlow()

    override fun observeEarthquake(id: String): Flow<Earthquake?> =
        storage.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun getCachedEarthquakes(): List<Earthquake> = storage.value

    override suspend fun refresh(range: TimeRange): Result<Int> {
        refreshCalls++
        lastRange = range
        refreshGate?.await()
        refreshError?.let { return Result.failure(it) }
        return refreshResult
    }
}
