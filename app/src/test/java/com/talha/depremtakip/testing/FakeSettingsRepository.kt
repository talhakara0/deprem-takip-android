package com.talha.depremtakip.testing

import com.talha.depremtakip.domain.model.UserSettings
import com.talha.depremtakip.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSettingsRepository(
    initial: UserSettings = UserSettings(),
    initialSeen: Set<String> = emptySet()
) : SettingsRepository {

    val settingsFlow = MutableStateFlow(initial)
    val seenFlow = MutableStateFlow(initialSeen)

    override val settings: Flow<UserSettings> = settingsFlow
    override val seenIds: Flow<Set<String>> = seenFlow

    override suspend fun setProvince(province: String) {
        settingsFlow.value = settingsFlow.value.copy(province = province)
    }

    override suspend fun setMagnitudeThreshold(threshold: Double) {
        settingsFlow.value = settingsFlow.value.copy(magnitudeThreshold = threshold)
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        settingsFlow.value = settingsFlow.value.copy(notificationsEnabled = enabled)
    }

    override suspend fun markSeen(ids: Set<String>) {
        seenFlow.value = seenFlow.value + ids
    }

    override suspend fun clearSeen() {
        seenFlow.value = emptySet()
    }
}
