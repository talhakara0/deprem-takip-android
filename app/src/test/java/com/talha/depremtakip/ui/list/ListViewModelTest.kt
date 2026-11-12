package com.talha.depremtakip.ui.list

import com.talha.depremtakip.domain.model.Earthquake
import com.talha.depremtakip.domain.model.TimeRange
import com.talha.depremtakip.domain.usecase.FilterEarthquakesUseCase
import com.talha.depremtakip.domain.usecase.GetEarthquakesUseCase
import com.talha.depremtakip.testing.FakeClock
import com.talha.depremtakip.testing.FakeEarthquakeRepository
import com.talha.depremtakip.testing.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Duration

@OptIn(ExperimentalCoroutinesApi::class)
class ListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val clock = FakeClock()
    private val now = clock.now()

    private fun createViewModel(repository: FakeEarthquakeRepository): ListViewModel =
        ListViewModel(
            getEarthquakes = GetEarthquakesUseCase(repository, clock),
            filterEarthquakes = FilterEarthquakesUseCase(),
            clock = clock
        )

    @Test
    fun `state starts loading then emits success`() = runTest {
        val flow = MutableSharedFlow<List<Earthquake>>()
        val repository = FakeEarthquakeRepository(
            initial = emptyList(),
            hotFlow = flow
        )
        repository.refreshGate = CompletableDeferred()

        val viewModel = createViewModel(repository)
        assertTrue(viewModel.state.value.isLoading)

        runCurrent()
        assertEquals(1, repository.refreshCalls)

        flow.emit(listOf(quake("a", 4.2, now.minus(Duration.ofMinutes(10)))))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(1, state.earthquakes.size)
        assertFalse(state.isOffline)
    }

    @Test
    fun `refresh failure shows cached data with offline flag`() = runTest {
        val repository = FakeEarthquakeRepository(
            initial = listOf(quake("cached", 3.5, now.minus(Duration.ofHours(1))))
        )
        repository.refreshError = IllegalStateException("offline")

        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.isOffline)
        assertEquals(listOf("cached"), state.earthquakes.map { it.id })
        assertFalse(state.isLoading)
    }

    @Test
    fun `successful refresh clears offline flag`() = runTest {
        val repository = FakeEarthquakeRepository(initial = listOf(quake("a", 3.0, now)))
        repository.refreshError = IllegalStateException("offline")

        val viewModel = createViewModel(repository)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.isOffline)

        repository.refreshError = null
        repository.refreshResult = Result.success(1)
        viewModel.refresh()
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isOffline)
        assertEquals(now, viewModel.state.value.lastUpdated)
    }

    @Test
    fun `filters are applied to cached list without new network call`() = runTest {
        val repository = FakeEarthquakeRepository(
            initial = listOf(
                quake("big", 5.0, now),
                quake("small", 2.0, now)
            )
        )
        val viewModel = createViewModel(repository)
        advanceUntilIdle()
        val callsBefore = repository.refreshCalls

        viewModel.setMinMagnitude(4.0)
        advanceUntilIdle()

        assertEquals(listOf("big"), viewModel.state.value.earthquakes.map { it.id })
        assertEquals(callsBefore, repository.refreshCalls)
    }

    @Test
    fun `changing range re-subscribes and refreshes`() = runTest {
        val repository = FakeEarthquakeRepository(
            initial = listOf(
                quake("recent", 3.0, now.minus(Duration.ofHours(2))),
                quake("old", 3.2, now.minus(Duration.ofDays(4)))
            )
        )
        val viewModel = createViewModel(repository)
        advanceUntilIdle()
        assertEquals(1, viewModel.state.value.earthquakes.size)

        viewModel.setRange(TimeRange.LAST_7_DAYS)
        advanceUntilIdle()

        assertEquals(TimeRange.LAST_7_DAYS, viewModel.state.value.range)
        assertEquals(TimeRange.LAST_7_DAYS, repository.lastRange)
        assertEquals(2, viewModel.state.value.earthquakes.size)
    }

    private fun quake(id: String, magnitude: Double, date: java.time.Instant) = Earthquake(
        id = id,
        province = "İzmir",
        magnitude = magnitude,
        date = date
    )
}
