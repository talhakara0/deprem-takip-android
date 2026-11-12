package com.talha.depremtakip.ui.detail

import androidx.lifecycle.SavedStateHandle
import com.talha.depremtakip.domain.model.Earthquake
import com.talha.depremtakip.domain.usecase.GetEarthquakeDetailUseCase
import com.talha.depremtakip.testing.FakeEarthquakeRepository
import com.talha.depremtakip.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `loads matching earthquake from cache`() = runTest {
        val repository = FakeEarthquakeRepository(
            listOf(Earthquake(id = "e1", province = "İzmir", magnitude = 4.1))
        )
        val viewModel = DetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("id" to "e1")),
            getEarthquakeDetail = GetEarthquakeDetailUseCase(repository)
        )

        assertTrue(viewModel.state.value.isLoading)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals("e1", viewModel.state.value.earthquake?.id)
    }

    @Test
    fun `unknown id results in no earthquake`() = runTest {
        val repository = FakeEarthquakeRepository(emptyList())
        val viewModel = DetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("id" to "missing")),
            getEarthquakeDetail = GetEarthquakeDetailUseCase(repository)
        )

        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.earthquake)
    }
}
