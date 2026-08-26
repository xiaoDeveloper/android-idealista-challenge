package com.xiao.idealistachallenge.data.remote

import kotlinx.serialization.decodeFromString
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class RemoteJsonSerializationTest {

    @Test
    fun `listing dto preserves a decimal price and ignores unknown fields`() {
        val ads = RemoteJson.instance.decodeFromString<List<PropertyAdDto>>(
            """
            [{
              "propertyCode": "ad-42",
              "price": 123456.78,
              "multimedia": { "images": [{ "url": "https://images.example/ad-42.jpg" }] },
              "newSourceField": "ignored"
            }]
            """.trimIndent(),
        )

        assertEquals("ad-42", ads.single().propertyCode)
        assertEquals(BigDecimal("123456.78"), ads.single().price)
        assertEquals("https://images.example/ad-42.jpg", ads.single().multimedia?.images?.single()?.url)
    }
}
