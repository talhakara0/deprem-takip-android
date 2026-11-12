package com.talha.depremtakip.data.repository

import com.talha.depremtakip.data.local.EarthquakeDao
import com.talha.depremtakip.data.local.toDomain
import com.talha.depremtakip.data.local.toEntity
import com.talha.depremtakip.data.remote.AfadApi
import com.talha.depremtakip.data.remote.dto.toDomain
import com.talha.depremtakip.di.IoDispatcher
import com.talha.depremtakip.domain.model.Earthquake
import com.talha.depremtakip.domain.model.TimeRange
import com.talha.depremtakip.domain.repository.EarthquakeRepository
import com.talha.depremtakip.domain.time.Clock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EarthquakeRepositoryImpl @Inject constructor(
    private val api: AfadApi,
    private val dao: EarthquakeDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val clock: Clock
) : EarthquakeRepository {

    override fun observeEarthquakes(): Flow<List<Earthquake>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeEarthquake(id: String): Flow<Earthquake?> =
        dao.observeById(id).map { it?.toDomain() }

    override suspend fun getCachedEarthquakes(): List<Earthquake> = withContext(ioDispatcher) {
        dao.getSnapshot().map { it.toDomain() }
    }

    override suspend fun refresh(range: TimeRange): Result<Int> = withContext(ioDispatcher) {
        runCatching {
            val now = clock.now()
            val start = now.minus(range.duration)
            val response = api.getEvents(
                start = AFAD_FORMATTER.format(start),
                end = AFAD_FORMATTER.format(now)
            )
            val entities = response.map { it.toDomain().toEntity() }
            if (entities.isNotEmpty()) {
                dao.upsertAll(entities)
            }
            dao.deleteOlderThan(now.minus(Duration.ofDays(8)).toEpochMilli())
            entities.size
        }
    }

    private companion object {
        val AFAD_ZONE: ZoneId = ZoneId.of("Europe/Istanbul")
        val AFAD_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").withZone(AFAD_ZONE)
    }
}
