package com.talha.depremtakip.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EarthquakeDao {

    @Query("SELECT * FROM earthquakes ORDER BY dateMillis DESC")
    fun observeAll(): Flow<List<EarthquakeEntity>>

    @Query("SELECT * FROM earthquakes WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<EarthquakeEntity?>

    @Query("SELECT * FROM earthquakes ORDER BY dateMillis DESC")
    suspend fun getSnapshot(): List<EarthquakeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(earthquakes: List<EarthquakeEntity>)

    @Query("DELETE FROM earthquakes WHERE dateMillis IS NOT NULL AND dateMillis < :cutoffMillis")
    suspend fun deleteOlderThan(cutoffMillis: Long)

    @Query("DELETE FROM earthquakes")
    suspend fun clear()
}
