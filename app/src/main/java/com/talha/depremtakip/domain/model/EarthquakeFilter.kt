package com.talha.depremtakip.domain.model

enum class SortOrder {
    TIME_DESCENDING,
    MAGNITUDE_DESCENDING
}

data class EarthquakeFilter(
    val minMagnitude: Double = 0.0,
    val query: String = "",
    val sortOrder: SortOrder = SortOrder.TIME_DESCENDING
)
