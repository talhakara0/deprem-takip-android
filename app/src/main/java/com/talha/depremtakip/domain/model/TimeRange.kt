package com.talha.depremtakip.domain.model

import java.time.Duration

enum class TimeRange(val duration: Duration) {
    LAST_24_HOURS(Duration.ofHours(24)),
    LAST_7_DAYS(Duration.ofDays(7))
}
