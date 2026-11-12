package com.talha.depremtakip.data.remote.dto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class EarthquakeMapperTest {

    @Test
    fun `maps all fields to domain`() {
        val dto = EarthquakeDto(
            eventID = "evt-1",
            location = "İzmir Körfezi",
            latitude = 38.4,
            longitude = 27.1,
            depth = 8.2,
            type = "ML",
            magnitude = 4.3,
            province = "İzmir",
            district = "Konak",
            date = "2026-11-12T09:30:00",
            isEventUpdate = true
        )

        val domain = dto.toDomain()

        assertEquals("evt-1", domain.id)
        assertEquals("İzmir Körfezi", domain.location)
        assertEquals(38.4, domain.latitude!!, 0.0001)
        assertEquals(4.3, domain.magnitude!!, 0.0001)
        assertEquals("İzmir", domain.province)
        assertEquals(Instant.parse("2026-11-12T06:30:00Z"), domain.date)
        assertFalse(domain.isUpdate.not())
    }

    @Test
    fun `missing optional fields become null`() {
        val domain = EarthquakeDto(eventID = "evt-2").toDomain()

        assertNull(domain.location)
        assertNull(domain.latitude)
        assertNull(domain.longitude)
        assertNull(domain.depth)
        assertNull(domain.magnitude)
        assertNull(domain.province)
        assertNull(domain.district)
        assertNull(domain.date)
        assertFalse(domain.isUpdate)
    }

    @Test
    fun `blank event id falls back to generated id`() {
        val dto = EarthquakeDto(
            eventID = " ",
            latitude = 38.0,
            longitude = 27.0,
            date = "2026-11-12T09:30:00"
        )

        val domain = dto.toDomain()

        assertEquals("38.0_27.0_2026-11-12T09:30:00", domain.id)
    }

    @Test
    fun `parses ISO instant with timezone marker`() {
        assertEquals(
            Instant.parse("2026-11-12T06:30:00Z"),
            parseAfadDate("2026-11-12T06:30:00Z")
        )
    }

    @Test
    fun `invalid date returns null and does not fail mapping`() {
        val domain = EarthquakeDto(eventID = "evt-3", date = "not-a-date").toDomain()

        assertNull(domain.date)
        assertNotNull(domain.id)
    }
}
