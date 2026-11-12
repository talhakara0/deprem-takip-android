package com.talha.depremtakip.domain.usecase

import com.talha.depremtakip.domain.model.Earthquake
import com.talha.depremtakip.domain.model.EarthquakeFilter
import com.talha.depremtakip.domain.model.SortOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class FilterEarthquakesUseCaseTest {

    private val useCase = FilterEarthquakesUseCase()

    private val t0 = Instant.parse("2026-11-12T10:00:00Z")
    private val quakes = listOf(
        quake("a", province = "İzmir", district = "Konak", magnitude = 4.5, date = t0.minusSeconds(600)),
        quake("b", province = "Ankara", district = "Çankaya", magnitude = 2.2, date = t0.minusSeconds(60)),
        quake("c", province = "İzmir", district = "Bornova", magnitude = 3.8, date = t0),
        quake("d", province = "Iğdır", district = null, magnitude = null, date = t0.minusSeconds(120))
    )

    @Test
    fun `filters by minimum magnitude`() {
        val result = useCase(quakes, EarthquakeFilter(minMagnitude = 3.8))

        assertEquals(listOf("c", "a"), result.map { it.id })
    }

    @Test
    fun `null magnitude is excluded when minimum is above zero`() {
        val result = useCase(quakes, EarthquakeFilter(minMagnitude = 0.5))

        assertTrue(result.none { it.id == "d" })
    }

    @Test
    fun `sorts by time descending by default`() {
        val result = useCase(quakes, EarthquakeFilter())

        assertEquals(listOf("c", "b", "d", "a"), result.map { it.id })
    }

    @Test
    fun `sorts by magnitude descending with time as tie breaker`() {
        val result = useCase(
            quakes,
            EarthquakeFilter(sortOrder = SortOrder.MAGNITUDE_DESCENDING)
        )

        assertEquals(listOf("a", "c", "b", "d"), result.map { it.id })
    }

    @Test
    fun `province query is Turkish case insensitive`() {
        val result = useCase(quakes, EarthquakeFilter(query = "izmir"))

        assertEquals(listOf("c", "a"), result.map { it.id })
    }

    @Test
    fun `query matches district as well`() {
        val result = useCase(quakes, EarthquakeFilter(query = "çankaya"))

        assertEquals(listOf("b"), result.map { it.id })
    }

    @Test
    fun `query with dotless i matches dotted i province`() {
        val result = useCase(quakes, EarthquakeFilter(query = "IGDIR"))

        assertEquals(listOf("d"), result.map { it.id })
    }

    @Test
    fun `blank query returns all`() {
        val result = useCase(quakes, EarthquakeFilter(query = "   "))

        assertEquals(4, result.size)
    }

    private fun quake(
        id: String,
        province: String?,
        district: String?,
        magnitude: Double?,
        date: Instant?
    ) = Earthquake(
        id = id,
        location = listOfNotNull(district, province).joinToString(", "),
        province = province,
        district = district,
        magnitude = magnitude,
        date = date
    )
}
