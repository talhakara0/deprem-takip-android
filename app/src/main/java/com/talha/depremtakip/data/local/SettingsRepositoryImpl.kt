package com.talha.depremtakip.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.talha.depremtakip.domain.model.UserSettings
import com.talha.depremtakip.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private val data: Flow<Preferences> = context.settingsDataStore.data.catch { error ->
        if (error is IOException) {
            emit(emptyPreferences())
        } else {
            throw error
        }
    }

    override val settings: Flow<UserSettings> = data.map { preferences ->
        UserSettings(
            province = preferences[Keys.PROVINCE].orEmpty(),
            magnitudeThreshold = preferences[Keys.THRESHOLD] ?: 4.0,
            notificationsEnabled = preferences[Keys.ENABLED] ?: false
        )
    }

    override val seenIds: Flow<Set<String>> = data.map { preferences ->
        preferences[Keys.SEEN_IDS].orEmpty()
    }

    override suspend fun setProvince(province: String) {
        context.settingsDataStore.edit { it[Keys.PROVINCE] = province.trim() }
    }

    override suspend fun setMagnitudeThreshold(threshold: Double) {
        context.settingsDataStore.edit { it[Keys.THRESHOLD] = threshold }
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.ENABLED] = enabled }
    }

    override suspend fun markSeen(ids: Set<String>) {
        if (ids.isEmpty()) return
        context.settingsDataStore.edit { preferences ->
            val existing = preferences[Keys.SEEN_IDS].orEmpty()
            preferences[Keys.SEEN_IDS] = existing + ids
        }
    }

    override suspend fun clearSeen() {
        context.settingsDataStore.edit { it.remove(Keys.SEEN_IDS) }
    }

    private object Keys {
        val PROVINCE = stringPreferencesKey("province")
        val THRESHOLD = doublePreferencesKey("magnitude_threshold")
        val ENABLED = booleanPreferencesKey("notifications_enabled")
        val SEEN_IDS = stringSetPreferencesKey("seen_ids")
    }
}
