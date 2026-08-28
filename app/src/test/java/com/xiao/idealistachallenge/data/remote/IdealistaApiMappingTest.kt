package com.xiao.idealistachallenge.data.remote

import java.math.BigDecimal
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class IdealistaApiMappingTest {

    private lateinit var server: MockWebServer
    private lateinit var api: IdealistaApi

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(
                RemoteJson.instance.asConverterFactory("application/json".toMediaType()),
            )
            .build()
            .create(IdealistaApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `listAds maps the dated observed fixture and requests the listing endpoint`() = runBlocking {
        server.enqueue(
            MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(fixture("fixtures/idealista/list-observed-2026-08-27.json")),
        )

        val ads = api.listAds()
        val request = server.takeRequest()

        assertEquals("GET", request.method)
        assertEquals("/list.json", request.path)
        assertEquals(4, ads.size)
        assertEquals("1", ads[0].propertyCode)
        assertEquals(BigDecimal("1195000.0"), ads[0].price)
        assertEquals(BigDecimal("1195000.0"), ads[0].priceInfo?.price?.amount)
        assertEquals("€", ads[0].priceInfo?.price?.currencySuffix)
        assertEquals(BigDecimal("133.0"), ads[0].size)
        assertEquals(false, ads[0].exterior)
        assertEquals(true, ads[0].features?.hasAirConditioning)
        assertEquals(false, ads[0].features?.hasBoxRoom)
        assertEquals(null, ads[0].parkingSpace)
        assertEquals("https://img4.idealista.com/blur/591_420_mq/0/id.pro.es.image.master/e1/0e/5e/1459427188.webp", ads[0].thumbnail)
        assertEquals("2", ads[1].propertyCode)
        assertEquals(BigDecimal("2750000.0"), ads[1].price)
        assertEquals(BigDecimal("1200.0"), ads[1].priceInfo?.price?.amount)
        assertEquals("€/mes", ads[1].priceInfo?.price?.currencySuffix)
        assertEquals(BigDecimal("241.0"), ads[1].size)
        assertEquals(true, ads[1].exterior)
        assertEquals(true, ads[1].features?.hasAirConditioning)
        assertEquals(true, ads[1].features?.hasBoxRoom)
        assertEquals(true, ads[1].parkingSpace?.hasParkingSpace)
        assertEquals(true, ads[1].parkingSpace?.isParkingSpaceIncludedInPrice)
        assertEquals(7, ads[1].multimedia?.images?.size)
        assertEquals("3", ads[2].propertyCode)
        assertEquals(true, ads[2].exterior)
        assertEquals(true, ads[2].features?.hasAirConditioning)
        assertEquals(true, ads[2].features?.hasBoxRoom)
        assertEquals(null, ads[2].parkingSpace)
        assertEquals("4", ads[3].propertyCode)
        assertEquals(false, ads[3].exterior)
        assertEquals(true, ads[3].features?.hasAirConditioning)
        assertEquals(false, ads[3].features?.hasBoxRoom)
        assertEquals(null, ads[3].parkingSpace)
    }

    @Test
    fun `listAds accepts an empty response`() = runBlocking {
        server.enqueue(
            MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("[]"),
        )

        assertEquals(emptyList<PropertyAdDto>(), api.listAds())
    }

    @Test
    fun `listAds exposes a non-success response as an HTTP error`() {
        server.enqueue(MockResponse().setResponseCode(503))

        val exception = assertThrows(HttpException::class.java) {
            runBlocking { api.listAds() }
        }

        assertEquals(503, exception.code())
    }

    private fun fixture(path: String): String = checkNotNull(
        javaClass.classLoader?.getResource(path),
    ).openStream().use { input ->
        input.readBytes().toString(StandardCharsets.UTF_8)
    }
}
