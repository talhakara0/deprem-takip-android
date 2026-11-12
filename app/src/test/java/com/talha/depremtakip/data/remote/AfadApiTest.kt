package com.talha.depremtakip.data.remote

import com.talha.depremtakip.data.remote.dto.toDomain
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.time.Instant

class AfadApiTest {

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun createApi(): AfadApi {
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            isLenient = true
            explicitNulls = false
        }
        return Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(AfadApi::class.java)
    }

    @Test
    fun `parses AFAD json array with unknown and missing fields`() {
        val body = """
            [
              {
                "eventID": "20261112-1",
                "location": "İzmir Körfezi",
                "latitude": 38.41,
                "longitude": 27.12,
                "depth": 7.5,
                "type": "ML",
                "magnitude": 4.2,
                "province": "İzmir",
                "date": "2026-11-12T09:15:00",
                "isEventUpdate": false,
                "unexpectedField": "ignored"
              },
              {
                "eventID": "20261112-2",
                "magnitude": 2.1
              }
            ]
        """.trimIndent()

        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(body)
        )

        val response = kotlinx.coroutines.runBlocking {
            createApi().getEvents(
                start = "2026-11-11T12:00:00",
                end = "2026-11-12T12:00:00"
            )
        }

        assertEquals(2, response.size)
        val first = response[0].toDomain()
        assertEquals("20261112-1", first.id)
        assertEquals("İzmir", first.province)
        assertEquals(4.2, first.magnitude!!, 0.0001)
        assertEquals(Instant.parse("2026-11-12T06:15:00Z"), first.date)
        assertNull(response[1].province)
        assertNull(response[1].date)

        val recorded = server.takeRequest()
        assertTrue(recorded.path!!.startsWith("/apiv2/event/filter?"))
        assertTrue(recorded.path!!.contains("minmag=2.0"))
    }

    @Test
    fun `empty array parses to empty list`() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("[]")
        )

        val response = kotlinx.coroutines.runBlocking {
            createApi().getEvents(
                start = "2026-11-11T12:00:00",
                end = "2026-11-12T12:00:00"
            )
        }

        assertTrue(response.isEmpty())
    }

    @Test
    fun `parses real AFAD payload where numbers are sent as strings`() {
        // Captured from the live endpoint on 2026-11-12: AFAD quotes every numeric field.
        val body = """
            [{"rms":"0.18","eventID":"734011","location":"Niksar (Tokat)","latitude":"40.59767",
              "longitude":"36.93417","depth":"6.67","type":"ML","magnitude":"2.7","country":"Türkiye",
              "province":"Tokat","district":"Niksar","neighborhood":"Fatih","date":"2026-11-11T23:31:55",
              "isEventUpdate":false,"lastUpdateDate":null}]
        """.trimIndent()
        server.enqueue(MockResponse().setBody(body).setHeader("Content-Type", "application/json"))

        val result = kotlinx.coroutines.runBlocking { createApi().getEvents(start = "s", end = "e") }

        val quake = result.single().toDomain()
        assertEquals("734011", quake.id)
        assertEquals(2.7, quake.magnitude!!, 0.0001)
        assertEquals(40.59767, quake.latitude!!, 0.00001)
        assertEquals(6.67, quake.depth!!, 0.0001)
        assertEquals("Tokat", quake.province)
    }
}
