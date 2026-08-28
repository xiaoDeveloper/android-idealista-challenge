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

    @Test
    fun `listing dto decodes highlight fields and ignores missing optional structures`() {
        val ads = RemoteJson.instance.decodeFromString<List<PropertyAdDto>>(
            """
            [
              {
                "propertyCode": "ad-full",
                "price": 100000.0,
                "exterior": true,
                "features": {
                  "hasAirConditioning": true,
                  "hasBoxRoom": true,
                  "hasSwimmingPool": true
                },
                "parkingSpace": {
                  "hasParkingSpace": true,
                  "isParkingSpaceIncludedInPrice": true
                }
              },
              {
                "propertyCode": "ad-empty",
                "price": 200000.0
              }
            ]
            """.trimIndent(),
        )

        val full = ads[0]
        assertEquals("ad-full", full.propertyCode)
        assertEquals(true, full.exterior)
        assertEquals(true, full.features?.hasAirConditioning)
        assertEquals(true, full.features?.hasBoxRoom)
        assertEquals(true, full.parkingSpace?.hasParkingSpace)
        assertEquals(true, full.parkingSpace?.isParkingSpaceIncludedInPrice)

        val empty = ads[1]
        assertEquals("ad-empty", empty.propertyCode)
        assertEquals(null, empty.exterior)
        assertEquals(null, empty.features)
        assertEquals(null, empty.parkingSpace)
    }
}
