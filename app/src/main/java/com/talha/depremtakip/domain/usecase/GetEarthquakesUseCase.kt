package com.talha.depremtakip.domain.usecase

import javax.inject.Inject
import com.talha.depremtakip.domain.model.Earthquake
import com.talha.depremtakip.domain.model.TimeRange
import com.talha.depremtakip.domain.repository.EarthquakeRepository
import com.talha.depremtakip.domain.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetEarthquakesUseCase @Inject constructor(
    private val repository: EarthquakeRepository,
    private val clock: Clock
) {

    operator fun invoke(range: TimeRange): Flow<List<Earthquake>> {
        val cutoff = clock.now().minus(range.duration)
        return repository.observeEarthquakes().map { earthquakes ->
            earthquakes.filter { quake ->
                val date = quake.date
                date == null || date.isAfter(cutoff)
            }
        }
    }

    suspend fun refresh(range: TimeRange): Result<Int> = repository.refresh(range)
}
