package com.talha.depremtakip.data.repository

import com.talha.depremtakip.data.local.EarthquakeDao
import com.talha.depremtakip.data.local.EarthquakeEntity
import com.talha.depremtakip.data.remote.AfadApi
import com.talha.depremtakip.data.remote.dto.EarthquakeDto
import com.talha.depremtakip.domain.model.TimeRange
import com.talha.depremtakip.domain.time.Clock
import com.talha.depremtakip.testing.FakeClock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.time.Duration
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class EarthquakeRepositoryImplTest {

    private val clock: Clock = FakeClock()
    private val now = clock.now()

    @Test
    fun `refresh success maps and stores entities`() = runTest {
        val api = FakeAfadApi().apply {
            response = listOf(
                EarthquakeDto(
                    eventID = "e1",
                    latitude = 38.4,
                    longitude = 27.1,
                    magnitude = 4.1,
                    province = "İzmir",
                    date = "2026-11-12T10:00:00"
                )
            )
        }
        val dao = FakeEarthquakeDao()
        val repository = EarthquakeRepositoryImpl(
            api = api,
            dao = dao,
            ioDispatcher = StandardTestDispatcher(testScheduler),
            clock = clock
        )

        val result = repository.refresh(TimeRange.LAST_24_HOURS)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull())
        assertEquals(1, dao.snapshot.size)
        assertEquals("e1", dao.snapshot.first().id)
        assertEquals(Instant.parse("2026-11-12T07:00:00Z").toEpochMilli(), dao.snapshot.first().dateMillis)
        assertTrue(api.lastStart!!.startsWith("2026-11-11T15:00:00"))
    }

    @Test
    fun `refresh failure keeps cache and returns failure`() = runTest {
        val dao = FakeEarthquakeDao(
            listOf(entity(id = "cached", date = now.minus(Duration.ofHours(2))))
        )
        val api = FakeAfadApi().apply { failure = IOException("offline") }
        val repository = EarthquakeRepositoryImpl(
            api = api,
            dao = dao,
            ioDispatcher = StandardTestDispatcher(testScheduler),
            clock = clock
        )

        val result = repository.refresh(TimeRange.LAST_24_HOURS)

        assertTrue(result.isFailure)
        assertEquals(1, dao.snapshot.size)
        assertEquals("cached", dao.snapshot.first().id)
    }

    @Test
    fun `observe maps cached entities to domain with dates`() = runTest {
        val timestamp = now.minus(Duration.ofMinutes(30))
        val dao = FakeEarthquakeDao(listOf(entity(id = "cached", date = timestamp)))
        val repository = EarthquakeRepositoryImpl(
            api = FakeAfadApi(),
            dao = dao,
            ioDispatcher = StandardTestDispatcher(testScheduler),
            clock = clock
        )

        val emitted = repository.observeEarthquakes().first()

        assertEquals(1, emitted.size)
        assertEquals(timestamp, emitted.first().date)
    }

    @Test
    fun `refresh prunes entries older than eight days`() = runTest {
        val dao = FakeEarthquakeDao(
            listOf(
                entity(id = "old", date = now.minus(Duration.ofDays(10))),
                entity(id = "recent", date = now.minus(Duration.ofHours(1)))
            )
        )
        val api = FakeAfadApi().apply {
            response = listOf(
                EarthquakeDto(
                    eventID = "new",
                    latitude = 38.0,
                    longitude = 27.0,
                    date = "2026-11-12T11:00:00"
                )
            )
        }
        val repository = EarthquakeRepositoryImpl(
            api = api,
            dao = dao,
            ioDispatcher = StandardTestDispatcher(testScheduler),
            clock = clock
        )

        repository.refresh(TimeRange.LAST_24_HOURS)

        assertEquals(setOf("recent", "new"), dao.snapshot.map { it.id }.toSet())
    }

    @Test
    fun `cached snapshot returns empty list when nothing stored`() = runTest {
        val repository = EarthquakeRepositoryImpl(
            api = FakeAfadApi(),
            dao = FakeEarthquakeDao(),
            ioDispatcher = StandardTestDispatcher(testScheduler),
            clock = clock
        )

        assertTrue(repository.getCachedEarthquakes().isEmpty())
    }

    private fun entity(id: String, date: Instant?): EarthquakeEntity = EarthquakeEntity(
        id = id,
        location = "Test",
        latitude = 38.0,
        longitude = 27.0,
        depth = 5.0,
        type = "ML",
        magnitude = 3.0,
        province = "İzmir",
        district = null,
        dateMillis = date?.toEpochMilli(),
        isUpdate = false
    )

    private class FakeAfadApi : AfadApi {
        var response: List<EarthquakeDto> = emptyList()
        var failure: Throwable? = null
        var lastStart: String? = null

        override suspend fun getEvents(
            start: String,
            end: String,
            minMagnitude: Double,
            orderBy: String,
            limit: Int
        ): List<EarthquakeDto> {
            lastStart = start
            failure?.let { throw it }
            return response
        }
    }

    private class FakeEarthquakeDao(
        initial: List<EarthquakeEntity> = emptyList()
    ) : EarthquakeDao {

        val state = MutableStateFlow(initial)
        val snapshot: List<EarthquakeEntity>
            get() = state.value

        override fun observeAll(): Flow<List<EarthquakeEntity>> =
            state.map { list -> list.sortedByDescending { it.dateMillis ?: Long.MIN_VALUE } }

        override fun observeById(id: String): Flow<EarthquakeEntity?> =
            state.map { list -> list.firstOrNull { it.id == id } }

        override suspend fun getSnapshot(): List<EarthquakeEntity> = state.value

        override suspend fun upsertAll(earthquakes: List<EarthquakeEntity>) {
            val merged = state.value.associateBy { it.id }.toMutableMap()
            earthquakes.forEach { merged[it.id] = it }
            state.value = merged.values.toList()
        }

        override suspend fun deleteOlderThan(cutoffMillis: Long) {
            state.value = state.value.filter {
                it.dateMillis == null || it.dateMillis >= cutoffMillis
            }
        }

        override suspend fun clear() {
            state.value = emptyList()
        }
    }
}
