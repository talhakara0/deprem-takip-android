package com.talha.depremtakip.domain.model

import java.time.Instant

data class Earthquake(
    val id: String,
    val location: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val depth: Double? = null,
    val type: String? = null,
    val magnitude: Double? = null,
    val province: String? = null,
    val district: String? = null,
    val date: Instant? = null,
    val isUpdate: Boolean = false
) {
    val hasCoordinates: Boolean
        get() = latitude != null && longitude != null
}
