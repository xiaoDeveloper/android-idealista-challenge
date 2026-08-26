package com.xiao.idealistachallenge.model

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PropertyModelsTest {

    @Test
    fun `property ad keeps the required summary values`() {
        val ad = PropertyAd(
            propertyCode = "P-42",
            thumbnailUrl = "https://images.example.test/thumbnail.jpg",
            price = BigDecimal("123456.78"),
            currencySuffix = "€",
            propertyType = "flat",
            address = "Calle Example 1",
            municipality = "Madrid",
            district = "Centro",
            sizeSquareMeters = 80,
            rooms = 3,
            bathrooms = 2,
            description = "Sunny home",
            imageUrls = listOf("https://images.example.test/one.jpg"),
        )

        assertEquals("P-42", ad.propertyCode)
        assertEquals(BigDecimal("123456.78"), ad.price)
        assertEquals(80, ad.sizeSquareMeters)
        assertEquals(listOf("https://images.example.test/one.jpg"), ad.imageUrls)
    }

    @Test
    fun `property ad rejects a blank local identity`() {
        val failure = runCatching {
            PropertyAd(
                propertyCode = " ",
                price = BigDecimal.ONE,
            )
        }.exceptionOrNull()

        assertTrue(failure is IllegalArgumentException)
    }

    @Test
    fun `property ad rejects negative optional counts`() {
        val failure = runCatching {
            PropertyAd(
                propertyCode = "P-42",
                price = BigDecimal.ONE,
                rooms = -1,
            )
        }.exceptionOrNull()

        assertTrue(failure is IllegalArgumentException)
    }

    @Test
    fun `details retain the selected listing identity separate from remote metadata`() {
        val details = PropertyDetails(
            selectedAdId = "P-42",
            remoteAdId = 1,
            price = BigDecimal("123456.78"),
            description = "Fixed endpoint content",
            imageUrls = listOf("https://images.example.test/one.jpg"),
            latitude = BigDecimal("40.4168"),
            longitude = BigDecimal("-3.7038"),
            characteristics = mapOf("rooms" to "3"),
        )

        assertEquals("P-42", details.selectedAdId)
        assertEquals(1, details.remoteAdId)
        assertEquals("3", details.characteristics.getValue("rooms"))
    }

    @Test
    fun `favorite stores the selected ad id and favorited instant`() {
        val favorite = Favorite(
            adId = "P-42",
            favoritedAtEpochMillis = 1_725_000_000_000L,
        )

        assertEquals("P-42", favorite.adId)
        assertEquals(1_725_000_000_000L, favorite.favoritedAtEpochMillis)
    }
}
