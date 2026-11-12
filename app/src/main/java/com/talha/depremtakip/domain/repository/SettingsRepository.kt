package com.talha.depremtakip.domain.repository

import com.talha.depremtakip.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<UserSettings>
    val seenIds: Flow<Set<String>>
    suspend fun setProvince(province: String)
    suspend fun setMagnitudeThreshold(threshold: Double)
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun markSeen(ids: Set<String>)
    suspend fun clearSeen()
}
