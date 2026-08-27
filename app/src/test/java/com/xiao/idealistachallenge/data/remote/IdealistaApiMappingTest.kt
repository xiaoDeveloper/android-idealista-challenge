package com.xiao.idealistachallenge.data.remote

import java.math.BigDecimal
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
    fun `listAds maps representative fields and requests the listing endpoint`() = runBlocking {
        server.enqueue(
            MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                    [{
                      "propertyCode": "ad-42",
                      "thumbnail": "https://images.example/ad-42-thumb.jpg",
                      "price": 123456.78,
                      "priceInfo": {
                        "price": 123456.78,
                        "amount": 123456.78,
                        "currencySuffix": "€"
                      },
                      "propertyType": "flat",
                      "address": "Calle Mayor, 42",
                      "municipality": "Madrid",
                      "district": "Centro",
                      "size": 85,
                      "rooms": 3,
                      "bathrooms": 2,
                      "description": "Bright central home",
                      "multimedia": {
                        "images": [{"url": "https://images.example/ad-42.jpg"}]
                      },
                      "unknownField": "ignored"
                    }]
                    """.trimIndent(),
                ),
        )

        val ad = api.listAds().single()
        val request = server.takeRequest()

        assertEquals("GET", request.method)
        assertEquals("/list.json", request.path)
        assertEquals("ad-42", ad.propertyCode)
        assertEquals("https://images.example/ad-42-thumb.jpg", ad.thumbnail)
        assertEquals(BigDecimal("123456.78"), ad.price)
        assertEquals(BigDecimal("123456.78"), ad.priceInfo?.price)
        assertEquals("€", ad.priceInfo?.currencySuffix)
        assertEquals("flat", ad.propertyType)
        assertEquals("Calle Mayor, 42", ad.address)
        assertEquals("Madrid", ad.municipality)
        assertEquals("Centro", ad.district)
        assertEquals(85, ad.size)
        assertEquals(3, ad.rooms)
        assertEquals(2, ad.bathrooms)
        assertEquals("Bright central home", ad.description)
        assertEquals("https://images.example/ad-42.jpg", ad.multimedia?.images?.single()?.url)
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
}
