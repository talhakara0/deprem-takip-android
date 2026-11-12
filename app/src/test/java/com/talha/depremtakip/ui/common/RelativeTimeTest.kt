package com.talha.depremtakip.ui.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant

class RelativeTimeTest {

    private val now = Instant.parse("2026-11-12T12:00:00Z")

    @Test
    fun `null date is unknown`() {
        assertEquals(RelativeTimeLabel.Unknown, relativeTimeLabel(null, now))
    }

    @Test
    fun `future or current instant is now`() {
        assertEquals(
            RelativeTimeLabel.Now,
            relativeTimeLabel(now.plus(Duration.ofMinutes(3)), now)
        )
    }

    @Test
    fun `twelve minutes ago`() {
        assertEquals(
            RelativeTimeLabel.Minutes(12),
            relativeTimeLabel(now.minus(Duration.ofMinutes(12)), now)
        )
    }

    @Test
    fun `three hours ago`() {
        assertEquals(
            RelativeTimeLabel.Hours(3),
            relativeTimeLabel(now.minus(Duration.ofHours(3)), now)
        )
    }

    @Test
    fun `two days ago`() {
        assertEquals(
            RelativeTimeLabel.Days(2),
            relativeTimeLabel(now.minus(Duration.ofDays(2)), now)
        )
    }

    @Test
    fun `just under an hour stays minutes`() {
        assertTrue(
            relativeTimeLabel(now.minus(Duration.ofMinutes(59)), now) is RelativeTimeLabel.Minutes
        )
    }
}
