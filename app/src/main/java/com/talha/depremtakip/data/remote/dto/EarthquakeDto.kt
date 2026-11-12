package com.talha.depremtakip.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class EarthquakeDto(
    val eventID: String? = null,
    val location: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val depth: Double? = null,
    val type: String? = null,
    val magnitude: Double? = null,
    val province: String? = null,
    val district: String? = null,
    val date: String? = null,
    val isEventUpdate: Boolean? = null
)
