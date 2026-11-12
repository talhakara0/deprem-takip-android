package com.talha.depremtakip.testing

import com.talha.depremtakip.domain.time.Clock
import java.time.Instant

class FakeClock(private val fixed: Instant = Instant.parse("2026-11-12T12:00:00Z")) : Clock {
    override fun now(): Instant = fixed
}
