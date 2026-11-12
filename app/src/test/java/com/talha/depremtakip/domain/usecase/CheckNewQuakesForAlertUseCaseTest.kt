package com.talha.depremtakip.domain.usecase

import com.talha.depremtakip.domain.model.Earthquake
import com.talha.depremtakip.domain.model.UserSettings
import com.talha.depremtakip.testing.FakeClock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant

class CheckNewQuakesForAlertUseCaseTest {

    private val clock = FakeClock()
    private val now: Instant = clock.now()
    private val useCase = CheckNewQuakesForAlertUseCase(clock)

    private val enabled = UserSettings(
        province = "izmir",
        magnitudeThreshold = 4.0,
        notificationsEnabled = true
    )

    @Test
    fun `returns matching new quakes sorted by time`() {
        val quakes = listOf(
            quake("old", "İzmir", 5.0, now.minus(Duration.ofHours(5))),
            quake("new", "İzmir", 4.6, now.minus(Duration.ofMinutes(10)))
        )

        val result = useCase(quakes, enabled, emptySet())

        assertEquals(listOf("new", "old"), result.map { it.id })
    }

    @Test
    fun `returns nothing when notifications disabled`() {
        val result = useCase(
            listOf(quake("a", "İzmir", 6.0, now.minus(Duration.ofMinutes(5)))),
            enabled.copy(notificationsEnabled = false),
            emptySet()
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `threshold boundary is inclusive`() {
        val result = useCase(
            listOf(quake("a", "İzmir", 4.0, now.minus(Duration.ofMinutes(5)))),
            enabled,
            emptySet()
        )

        assertEquals(listOf("a"), result.map { it.id })
    }

    @Test
    fun `below threshold is ignored`() {
        val result = useCase(
            listOf(quake("a", "İzmir", 3.99, now.minus(Duration.ofMinutes(5)))),
            enabled,
            emptySet()
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `already seen quakes are not alerted`() {
        val result = useCase(
            listOf(quake("a", "İzmir", 5.0, now.minus(Duration.ofMinutes(5)))),
            enabled,
            setOf("a")
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `turkish upper case province matches lower case setting`() {
        val result = useCase(
            listOf(quake("a", "İZMİR", 5.0, now.minus(Duration.ofMinutes(5)))),
            enabled,
            emptySet()
        )

        assertEquals(listOf("a"), result.map { it.id })
    }

    @Test
    fun `different province is ignored`() {
        val result = useCase(
            listOf(quake("a", "Ankara", 6.0, now.minus(Duration.ofMinutes(5)))),
            enabled,
            emptySet()
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `blank province means whole country`() {
        val result = useCase(
            listOf(
                quake("a", "Ankara", 5.0, now.minus(Duration.ofMinutes(5))),
                quake("b", "İzmir", 5.0, now.minus(Duration.ofMinutes(5)))
            ),
            enabled.copy(province = ""),
            emptySet()
        )

        assertEquals(2, result.size)
    }

    @Test
    fun `quakes older than alert window are ignored`() {
        val result = useCase(
            listOf(quake("a", "İzmir", 5.0, now.minus(Duration.ofHours(30)))),
            enabled,
            emptySet()
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `quakes without magnitude or date are ignored`() {
        val result = useCase(
            listOf(
                quake("a", "İzmir", null, now.minus(Duration.ofMinutes(5))),
                quake("b", "İzmir", 5.0, null)
            ),
            enabled,
            emptySet()
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `district match triggers alert`() {
        val result = useCase(
            listOf(
                Earthquake(
                    id = "a",
                    province = "Aydın",
                    district = "İzmir",
                    magnitude = 5.0,
                    date = now.minus(Duration.ofMinutes(5))
                )
            ),
            enabled,
            emptySet()
        )

        assertEquals(listOf("a"), result.map { it.id })
    }

    private fun quake(
        id: String,
        province: String,
        magnitude: Double?,
        date: Instant?
    ) = Earthquake(
        id = id,
        location = "$province merkez",
        province = province,
        magnitude = magnitude,
        date = date
    )
}
