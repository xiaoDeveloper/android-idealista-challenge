package com.xiao.idealistachallenge.data.remote

import java.math.BigDecimal
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class DetailMappingTest {
    private lateinit var server: MockWebServer
    private lateinit var api: IdealistaApi

    @Before fun setUp() {
        server = MockWebServer()
        server.start()
        api = Retrofit.Builder().baseUrl(server.url("/"))
            .addConverterFactory(RemoteJson.instance.asConverterFactory("application/json".toMediaType()))
            .build().create(IdealistaApi::class.java)
    }

    @After fun tearDown() = server.shutdown()

    @Test fun `getDetails maps the dated observed fixture and requests the fixed endpoint`() = runBlocking {
        server.enqueue(MockResponse().setHeader("Content-Type", "application/json")
            .setBody(fixture("fixtures/idealista/detail-observed-2026-08-27.json")))

        val detail = api.getDetails()
        val request = server.takeRequest()

        assertEquals("GET", request.method)
        assertEquals("/detail.json", request.path)
        assertEquals(1, detail.adid)
        assertEquals(BigDecimal("1195000.0"), detail.price)
        assertEquals(BigDecimal("1195000.0"), detail.priceInfo?.amount)
        assertEquals("€", detail.priceInfo?.currencySuffix)
        assertEquals("homes", detail.propertyType)
        assertEquals("flat", detail.extendedPropertyType)
        assertEquals("flat", detail.homeType)
        assertEquals("sale", detail.operation)
        assertEquals(10, detail.multimedia?.images?.size)
        assertEquals("livingRoom", detail.multimedia?.images?.first()?.tag)
        assertEquals("Salón", detail.multimedia?.images?.first()?.localizedName)
        assertEquals(
            "Zonas comunes",
            detail.multimedia?.images?.first { it.tag == "communalareas" }?.localizedName,
        )
        assertTrue(detail.multimedia?.images?.any { it.tag == "communalareas" } == true)
        assertEquals(BigDecimal("40.4362687"), detail.ubication?.latitude)
        assertEquals(BigDecimal("-3.6833686"), detail.ubication?.longitude)
        assertTrue(detail.propertyComment?.contains("Barrio de Salamanca") == true)
        assertTrue("The long observed description must remain intact", (detail.propertyComment?.length ?: 0) > 2_000)
        assertEquals("3", detail.moreCharacteristics.getValue("roomNumber").toString())
        assertEquals("e", detail.energyCertification?.energyConsumption?.type)
        assertEquals("e", detail.energyCertification?.emissions?.type)
    }

    private fun fixture(path: String): String = checkNotNull(javaClass.classLoader?.getResource(path))
        .openStream().use { it.readBytes().toString(StandardCharsets.UTF_8) }
}
