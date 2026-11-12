package com.talha.depremtakip.domain.repository

import com.talha.depremtakip.domain.model.Earthquake
import com.talha.depremtakip.domain.model.TimeRange
import kotlinx.coroutines.flow.Flow

interface EarthquakeRepository {
    fun observeEarthquakes(): Flow<List<Earthquake>>
    fun observeEarthquake(id: String): Flow<Earthquake?>
    suspend fun getCachedEarthquakes(): List<Earthquake>
    suspend fun refresh(range: TimeRange): Result<Int>
}
