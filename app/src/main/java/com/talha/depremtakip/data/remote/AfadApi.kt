package com.talha.depremtakip.data.remote

import com.talha.depremtakip.data.remote.dto.EarthquakeDto
import retrofit2.http.GET
import retrofit2.http.Query

interface AfadApi {

    @GET("apiv2/event/filter")
    suspend fun getEvents(
        @Query("start") start: String,
        @Query("end") end: String,
        @Query("minmag") minMagnitude: Double = 2.0,
        @Query("orderby") orderBy: String = "timedesc",
        @Query("limit") limit: Int = 200
    ): List<EarthquakeDto>
}
