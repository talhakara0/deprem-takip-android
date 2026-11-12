package com.talha.depremtakip.domain.model

data class UserSettings(
    val province: String = "",
    val magnitudeThreshold: Double = 4.0,
    val notificationsEnabled: Boolean = false
)
