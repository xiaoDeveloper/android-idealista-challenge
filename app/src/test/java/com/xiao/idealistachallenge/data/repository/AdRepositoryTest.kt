package com.xiao.idealistachallenge.data.repository

import com.xiao.idealistachallenge.data.remote.IdealistaApi
import com.xiao.idealistachallenge.data.remote.ImageDto
import com.xiao.idealistachallenge.data.remote.MultimediaDto
import com.xiao.idealistachallenge.data.remote.PriceInfoDto
import com.xiao.idealistachallenge.data.remote.PropertyAdDto
import com.xiao.idealistachallenge.data.remote.PropertyDetailsDto
import com.xiao.idealistachallenge.model.PropertyAd
import java.io.IOException
import java.math.BigDecimal
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdRepositoryTest {

    @Test
    fun `loadAds maps source ads into property ad models`() = runBlocking {
        val repository = AdRepository(
            FakeIdealistaApi(
                ads = listOf(
                    PropertyAdDto(
                        propertyCode = "ad-42",
                        thumbnail = "https://images.example/ad-42-thumbnail.jpg",
                        price = BigDecimal("123456.78"),
                        priceInfo = PriceInfoDto(currencySuffix = "€"),
                        propertyType = "flat",
                        address = "Calle Mayor 1",
                        municipality = "Madrid",
                        district = "Centro",
                        size = 72,
                        rooms = 3,
                        bathrooms = 2,
                        description = "A bright apartment near the city centre.",
                        multimedia = MultimediaDto(
                            images = listOf(
                                ImageDto(url = "https://images.example/ad-42-1.jpg"),
                                ImageDto(url = "https://images.example/ad-42-2.jpg"),
                            ),
                        ),
                    ),
                ),
            ),
        )

        val result = repository.loadAds()

        assertTrue(result.isSuccess)
        assertEquals(
            listOf(
                PropertyAd(
                    propertyCode = "ad-42",
                    thumbnailUrl = "https://images.example/ad-42-thumbnail.jpg",
                    price = BigDecimal("123456.78"),
                    currencySuffix = "€",
                    propertyType = "flat",
                    address = "Calle Mayor 1",
                    municipality = "Madrid",
                    district = "Centro",
                    sizeSquareMeters = 72,
                    rooms = 3,
                    bathrooms = 2,
                    description = "A bright apartment near the city centre.",
                    imageUrls = listOf(
                        "https://images.example/ad-42-1.jpg",
                        "https://images.example/ad-42-2.jpg",
                    ),
                ),
            ),
            result.getOrNull(),
        )
    }

    @Test
    fun `loadAds returns an empty successful result when the source is empty`() = runBlocking {
        val repository = AdRepository(FakeIdealistaApi(ads = emptyList()))

        val result = repository.loadAds()

        assertTrue(result.isSuccess)
        assertEquals(emptyList<PropertyAd>(), result.getOrNull())
    }

    @Test
    fun `loadAds returns a failure when an ad has no property identity`() = runBlocking {
        val repository = AdRepository(
            FakeIdealistaApi(
                ads = listOf(PropertyAdDto(propertyCode = null, price = BigDecimal("100000"))),
            ),
        )

        val result = repository.loadAds()

        assertTrue(result.isFailure)
    }

    @Test
    fun `loadAds returns a failure when an ad has no required price`() = runBlocking {
        val repository = AdRepository(
            FakeIdealistaApi(
                ads = listOf(PropertyAdDto(propertyCode = "ad-42", price = null)),
            ),
        )

        val result = repository.loadAds()

        assertTrue(result.isFailure)
    }

    @Test
    fun `loadAds returns a failure when the source request fails`() = runBlocking {
        val repository = AdRepository(
            FakeIdealistaApi(failure = IOException("network unavailable")),
        )

        val result = repository.loadAds()

        assertTrue(result.isFailure)
    }
}

private class FakeIdealistaApi(
    private val ads: List<PropertyAdDto> = emptyList(),
    private val failure: Throwable? = null,
) : IdealistaApi {

    override suspend fun listAds(): List<PropertyAdDto> {
        failure?.let { throw it }
        return ads
    }

    override suspend fun getDetails(): PropertyDetailsDto {
        throw UnsupportedOperationException("Detail endpoint is not used by listing tests")
    }
}
