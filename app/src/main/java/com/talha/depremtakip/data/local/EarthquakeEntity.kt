package com.talha.depremtakip.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.talha.depremtakip.domain.model.Earthquake
import java.time.Instant

@Entity(tableName = "earthquakes")
data class EarthquakeEntity(
    @PrimaryKey val id: String,
    val location: String?,
    val latitude: Double?,
    val longitude: Double?,
    val depth: Double?,
    val type: String?,
    val magnitude: Double?,
    val province: String?,
    val district: String?,
    val dateMillis: Long?,
    val isUpdate: Boolean
)

fun EarthquakeEntity.toDomain(): Earthquake = Earthquake(
    id = id,
    location = location,
    latitude = latitude,
    longitude = longitude,
    depth = depth,
    type = type,
    magnitude = magnitude,
    province = province,
    district = district,
    date = dateMillis?.let(Instant::ofEpochMilli),
    isUpdate = isUpdate
)

fun Earthquake.toEntity(): EarthquakeEntity = EarthquakeEntity(
    id = id,
    location = location,
    latitude = latitude,
    longitude = longitude,
    depth = depth,
    type = type,
    magnitude = magnitude,
    province = province,
    district = district,
    dateMillis = date?.toEpochMilli(),
    isUpdate = isUpdate
)
