package com.talha.depremtakip.ui.common

import java.time.Duration
import java.time.Instant

sealed interface RelativeTimeLabel {
    data object Unknown : RelativeTimeLabel
    data object Now : RelativeTimeLabel
    data class Minutes(val value: Long) : RelativeTimeLabel
    data class Hours(val value: Long) : RelativeTimeLabel
    data class Days(val value: Long) : RelativeTimeLabel
}

fun relativeTimeLabel(date: Instant?, now: Instant): RelativeTimeLabel {
    if (date == null) return RelativeTimeLabel.Unknown
    val minutes = Duration.between(date, now).toMinutes()
    return when {
        minutes <= 0 -> RelativeTimeLabel.Now
        minutes < 60 -> RelativeTimeLabel.Minutes(minutes)
        minutes < 24 * 60 -> RelativeTimeLabel.Hours(minutes / 60)
        else -> RelativeTimeLabel.Days(minutes / (24 * 60))
    }
}
