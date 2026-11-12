package com.talha.depremtakip.domain.usecase

import com.talha.depremtakip.domain.model.Earthquake
import com.talha.depremtakip.domain.model.TimeRange
import com.talha.depremtakip.testing.FakeClock
import com.talha.depremtakip.testing.FakeEarthquakeRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration

class GetEarthquakesUseCaseTest {

    private val clock = FakeClock()
    private val now = clock.now()

    private val recent = Earthquake(
        id = "recent",
        date = now.minus(Duration.ofHours(1))
    )
    private val threeDaysOld = Earthquake(
        id = "three-days",
        date = now.minus(Duration.ofDays(3))
    )
    private val noDate = Earthquake(id = "no-date", date = null)

    @Test
    fun `last 24 hours filters older entries but keeps unknown dates`() = runTest {
        val repository = FakeEarthquakeRepository(listOf(recent, threeDaysOld, noDate))
        val useCase = GetEarthquakesUseCase(repository, clock)

        val result = useCase(TimeRange.LAST_24_HOURS).first()

        assertEquals(setOf("recent", "no-date"), result.map { it.id }.toSet())
    }

    @Test
    fun `last 7 days includes three day old entry`() = runTest {
        val repository = FakeEarthquakeRepository(listOf(recent, threeDaysOld, noDate))
        val useCase = GetEarthquakesUseCase(repository, clock)

        val result = useCase(TimeRange.LAST_7_DAYS).first()

        assertEquals(3, result.size)
    }

    @Test
    fun `refresh delegates range to repository`() = runTest {
        val repository = FakeEarthquakeRepository()
        repository.refreshResult = Result.success(7)
        val useCase = GetEarthquakesUseCase(repository, clock)

        val result = useCase.refresh(TimeRange.LAST_7_DAYS)

        assertTrue(result.isSuccess)
        assertEquals(7, result.getOrNull())
        assertEquals(TimeRange.LAST_7_DAYS, repository.lastRange)
        assertEquals(1, repository.refreshCalls)
    }
}
