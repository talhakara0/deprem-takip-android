package com.talha.depremtakip.data.remote.dto

import com.talha.depremtakip.domain.model.Earthquake
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val AFAD_ZONE: ZoneId = ZoneId.of("Europe/Istanbul")

fun parseAfadDate(raw: String?): Instant? {
    val value = raw?.trim().orEmpty()
    if (value.isEmpty()) return null
    return runCatching { Instant.parse(value) }.getOrNull()
        ?: runCatching {
            LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                .atZone(AFAD_ZONE)
                .toInstant()
        }.getOrNull()
        ?: runCatching {
            LocalDateTime.parse(value.replace(' ', 'T'), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                .atZone(AFAD_ZONE)
                .toInstant()
        }.getOrNull()
}

fun EarthquakeDto.toDomain(): Earthquake {
    val parsedDate = parseAfadDate(date)
    val resolvedId = eventID?.takeIf { it.isNotBlank() }
        ?: "${latitude ?: "x"}_${longitude ?: "x"}_${date ?: "unknown"}"

    return Earthquake(
        id = resolvedId,
        location = location?.takeIf { it.isNotBlank() },
        latitude = latitude,
        longitude = longitude,
        depth = depth,
        type = type?.takeIf { it.isNotBlank() },
        magnitude = magnitude,
        province = province?.takeIf { it.isNotBlank() },
        district = district?.takeIf { it.isNotBlank() },
        date = parsedDate,
        isUpdate = isEventUpdate ?: false
    )
}
