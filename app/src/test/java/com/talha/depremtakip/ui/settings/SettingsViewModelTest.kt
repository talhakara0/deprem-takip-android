package com.talha.depremtakip.ui.settings

import com.talha.depremtakip.domain.model.UserSettings
import com.talha.depremtakip.testing.FakeSettingsRepository
import com.talha.depremtakip.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `emits persisted settings`() = runTest {
        val repository = FakeSettingsRepository(
            initial = UserSettings(
                province = "İzmir",
                magnitudeThreshold = 4.5,
                notificationsEnabled = true
            )
        )
        val viewModel = SettingsViewModel(repository)

        val job = launch { viewModel.state.collect { } }
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals("İzmir", viewModel.state.value.settings.province)
        assertEquals(4.5, viewModel.state.value.settings.magnitudeThreshold, 0.0001)
        assertTrue(viewModel.state.value.settings.notificationsEnabled)
        job.cancel()
    }

    @Test
    fun `updates are written to repository`() = runTest {
        val repository = FakeSettingsRepository()
        val viewModel = SettingsViewModel(repository)
        val job = launch { viewModel.state.collect { } }
        advanceUntilIdle()

        viewModel.setProvince("Ankara")
        viewModel.setThreshold(5.0)
        viewModel.setNotificationsEnabled(true)
        advanceUntilIdle()

        assertEquals("Ankara", repository.settingsFlow.value.province)
        assertEquals(5.0, repository.settingsFlow.value.magnitudeThreshold, 0.0001)
        assertTrue(repository.settingsFlow.value.notificationsEnabled)
        job.cancel()
    }
}
