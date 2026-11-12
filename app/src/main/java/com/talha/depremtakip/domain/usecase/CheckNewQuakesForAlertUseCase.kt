package com.talha.depremtakip.domain.usecase

import javax.inject.Inject
import com.talha.depremtakip.core.TurkishText
import com.talha.depremtakip.domain.model.Earthquake
import com.talha.depremtakip.domain.model.UserSettings
import com.talha.depremtakip.domain.time.Clock
import java.time.Duration

class CheckNewQuakesForAlertUseCase @Inject constructor(
    private val clock: Clock
) {

    operator fun invoke(
        earthquakes: List<Earthquake>,
        settings: UserSettings,
        seenIds: Set<String>
    ): List<Earthquake> {
        if (!settings.notificationsEnabled) return emptyList()

        val cutoff = clock.now().minus(ALERT_WINDOW)

        return earthquakes
            .filter { quake ->
                val magnitude = quake.magnitude
                val date = quake.date
                magnitude != null &&
                    magnitude >= settings.magnitudeThreshold &&
                    date != null &&
                    date.isAfter(cutoff) &&
                    quake.id !in seenIds &&
                    matchesProvince(quake, settings.province)
            }
            .sortedByDescending { it.date }
    }

    private fun matchesProvince(quake: Earthquake, province: String): Boolean {
        if (province.isBlank()) return true
        return TurkishText.contains(quake.province, province) ||
            TurkishText.contains(quake.district, province) ||
            TurkishText.contains(quake.location, province)
    }

    companion object {
        val ALERT_WINDOW: Duration = Duration.ofHours(24)
    }
}
