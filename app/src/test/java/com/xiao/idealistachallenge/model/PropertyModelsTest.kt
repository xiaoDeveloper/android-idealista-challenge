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
            images = listOf(
                PropertyImage(
                    url = "https://images.example.test/one.jpg",
                    semanticTag = PropertyImageTag.LIVING_ROOM,
                ),
            ),
        )

        assertEquals("P-42", ad.propertyCode)
        assertEquals(BigDecimal("123456.78"), ad.price)
        assertEquals(80, ad.sizeSquareMeters)
        assertEquals(
            listOf(
                PropertyImage(
                    url = "https://images.example.test/one.jpg",
                    semanticTag = PropertyImageTag.LIVING_ROOM,
                ),
            ),
            ad.images,
        )
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
    fun `details retain selected identity and only typed supported facts`() {
        val details = PropertyDetails(
            selectedAdId = "P-42",
            remoteAdId = 1,
            price = BigDecimal("123456.78"),
            description = "Fixed endpoint content",
            images = listOf(PropertyImage("https://images.example.test/one.jpg")),
            currencySuffix = "€",
            propertyType = "flat",
            operation = "sale",
            constructedAreaSquareMeters = 133,
            rooms = 3,
            bathrooms = 2,
            floor = "2",
            isExterior = false,
            hasLift = true,
            hasStorageRoom = false,
            isDuplex = false,
            communityCosts = BigDecimal("330"),
            energyConsumptionRating = EnergyRating.E,
            energyEmissionsRating = EnergyRating.E,
        )

        assertEquals("P-42", details.selectedAdId)
        assertEquals(1, details.remoteAdId)
        assertEquals("€", details.currencySuffix)
        assertEquals("flat", details.propertyType)
        assertEquals(133, details.constructedAreaSquareMeters)
        assertEquals(false, details.isExterior)
        assertEquals(EnergyRating.E, details.energyConsumptionRating)
    }

    @Test
    fun `image tags and energy ratings remain closed to supported values`() {
        assertEquals(PropertyImageTag.BEDROOM, PropertyImageTag.fromRemote("bedroom"))
        assertEquals(null, PropertyImageTag.fromRemote("communalareas"))
        assertEquals(EnergyRating.G, EnergyRating.fromRemote("g"))
        assertEquals(null, EnergyRating.fromRemote("not-rated"))
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
