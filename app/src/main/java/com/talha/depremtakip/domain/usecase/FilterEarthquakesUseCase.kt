package com.talha.depremtakip.domain.usecase

import javax.inject.Inject
import com.talha.depremtakip.domain.model.Earthquake
import com.talha.depremtakip.domain.model.SortOrder
import com.talha.depremtakip.core.TurkishText
import com.talha.depremtakip.domain.model.EarthquakeFilter
import java.time.Instant

class FilterEarthquakesUseCase @Inject constructor() {

    operator fun invoke(
        earthquakes: List<Earthquake>,
        filter: EarthquakeFilter
    ): List<Earthquake> {
        val filtered = earthquakes.filter { quake ->
            val magnitude = quake.magnitude ?: 0.0
            magnitude >= filter.minMagnitude && matchesQuery(quake, filter.query)
        }

        return when (filter.sortOrder) {
            SortOrder.TIME_DESCENDING -> filtered.sortedWith(
                compareByDescending<Earthquake> { it.date ?: Instant.MIN }
                    .thenByDescending { it.magnitude ?: 0.0 }
            )
            SortOrder.MAGNITUDE_DESCENDING -> filtered.sortedWith(
                compareByDescending<Earthquake> { it.magnitude ?: Double.NEGATIVE_INFINITY }
                    .thenByDescending { it.date ?: Instant.MIN }
            )
        }
    }

    private fun matchesQuery(quake: Earthquake, query: String): Boolean {
        if (query.isBlank()) return true
        return TurkishText.contains(quake.province, query) ||
            TurkishText.contains(quake.district, query) ||
            TurkishText.contains(quake.location, query)
    }
}
