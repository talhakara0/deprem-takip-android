package com.talha.depremtakip.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.talha.depremtakip.domain.model.TimeRange
import com.talha.depremtakip.domain.repository.EarthquakeRepository
import com.talha.depremtakip.domain.repository.SettingsRepository
import com.talha.depremtakip.domain.usecase.CheckNewQuakesForAlertUseCase
import com.talha.depremtakip.domain.usecase.GetEarthquakesUseCase
import com.talha.depremtakip.notification.QuakeNotifier
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class QuakeAlertWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val getEarthquakes: GetEarthquakesUseCase,
    private val checkNewQuakes: CheckNewQuakesForAlertUseCase,
    private val earthquakeRepository: EarthquakeRepository,
    private val settingsRepository: SettingsRepository,
    private val notifier: QuakeNotifier
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val refresh = getEarthquakes.refresh(TimeRange.LAST_24_HOURS)
        if (refresh.isFailure) {
            return if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.success()
        }

        notifier.ensureChannel()
        val settings = settingsRepository.settings.first()
        val seenIds = settingsRepository.seenIds.first()
        val earthquakes = earthquakeRepository.getCachedEarthquakes()

        val alerts = checkNewQuakes(earthquakes, settings, seenIds)
        alerts.forEach { notifier.notify(it) }

        settingsRepository.markSeen(earthquakes.map { it.id }.toSet())

        return Result.success()
    }

    companion object {
        const val WORK_NAME = "quake-alert-check"
        private const val MAX_RETRIES = 3
    }
}
