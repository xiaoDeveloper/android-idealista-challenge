package com.xiao.idealistachallenge.data.repository

import com.xiao.idealistachallenge.data.remote.DetailPriceInfoDto
import com.xiao.idealistachallenge.data.remote.EnergyCertificationDto
import com.xiao.idealistachallenge.data.remote.EnergyGradeDto
import com.xiao.idealistachallenge.data.remote.ImageDto
import com.xiao.idealistachallenge.data.remote.IdealistaApi
import com.xiao.idealistachallenge.data.remote.MultimediaDto
import com.xiao.idealistachallenge.data.remote.PriceInfoDto
import com.xiao.idealistachallenge.data.remote.PriceValueDto
import com.xiao.idealistachallenge.data.remote.PropertyAdDto
import com.xiao.idealistachallenge.data.remote.PropertyDetailsDto
import com.xiao.idealistachallenge.model.EnergyRating
import com.xiao.idealistachallenge.model.PropertyAd
import com.xiao.idealistachallenge.model.PropertyDetails
import com.xiao.idealistachallenge.model.PropertyImage
import com.xiao.idealistachallenge.model.PropertyImageTag
import java.io.IOException
import java.math.BigDecimal
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdRepositoryTest {

    @Test
    fun `loadAds prefers the nested amount and keeps its currency suffix`() = runBlocking {
        val repository = AdRepository(
            FakeIdealistaApi(
                ads = listOf(
                    PropertyAdDto(
                        propertyCode = "ad-42",
                        thumbnail = "https://images.example/ad-42-thumbnail.jpg",
                        price = BigDecimal("2750000.0"),
                        priceInfo = PriceInfoDto(
                            price = PriceValueDto(
                                amount = BigDecimal("1200.0"),
                                currencySuffix = "€/mes",
                            ),
                        ),
                        propertyType = "flat",
                        address = "Calle Mayor 1",
                        municipality = "Madrid",
                        district = "Centro",
                        size = BigDecimal("72.0"),
                        rooms = 3,
                        bathrooms = 2,
                        description = "A bright apartment near the city centre.",
                        multimedia = MultimediaDto(
                            images = listOf(
                                ImageDto(url = "https://images.example/ad-42-1.jpg", tag = "livingRoom"),
                                ImageDto(url = " ", tag = "bedroom"),
                                ImageDto(url = "https://images.example/ad-42-2.jpg", tag = "unrecognized"),
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
                    price = BigDecimal("1200.0"),
                    currencySuffix = "€/mes",
                    propertyType = "flat",
                    address = "Calle Mayor 1",
                    municipality = "Madrid",
                    district = "Centro",
                    sizeSquareMeters = 72,
                    rooms = 3,
                    bathrooms = 2,
                    description = "A bright apartment near the city centre.",
                    images = listOf(
                        PropertyImage(
                            url = "https://images.example/ad-42-1.jpg",
                            semanticTag = PropertyImageTag.LIVING_ROOM,
                        ),
                        PropertyImage(
                            url = "https://images.example/ad-42-2.jpg",
                            semanticTag = null,
                        ),
                    ),
                ),
            ),
            result.getOrNull(),
        )
    }

    @Test
    fun `loadAds falls back to top-level price without a nested suffix`() = runBlocking {
        val result = AdRepository(
            FakeIdealistaApi(
                ads = listOf(
                    PropertyAdDto(
                        propertyCode = "ad-fallback",
                        price = BigDecimal("2750000.0"),
                        priceInfo = PriceInfoDto(),
                    ),
                ),
            ),
        ).loadAds()

        assertEquals(BigDecimal("2750000.0"), result.getOrNull()?.single()?.price)
        assertEquals(null, result.getOrNull()?.single()?.currencySuffix)
    }

    @Test
    fun `loadAds normalizes only non-negative exact integral sizes in Int range`() = runBlocking {
        val result = AdRepository(
            FakeIdealistaApi(
                ads = listOf(
                    PropertyAdDto("valid", price = BigDecimal("1"), size = BigDecimal("72.000")),
                    PropertyAdDto("fractional", price = BigDecimal("1"), size = BigDecimal("72.5")),
                    PropertyAdDto("negative", price = BigDecimal("1"), size = BigDecimal("-1.0")),
                    PropertyAdDto("out-of-range", price = BigDecimal("1"), size = BigDecimal("2147483648.0")),
                ),
            ),
        ).loadAds()

        assertTrue(result.isSuccess)
        assertEquals(72, result.getOrNull()?.first()?.sizeSquareMeters)
        assertEquals(null, result.getOrNull()?.get(1)?.sizeSquareMeters)
        assertEquals(null, result.getOrNull()?.get(2)?.sizeSquareMeters)
        assertEquals(null, result.getOrNull()?.get(3)?.sizeSquareMeters)
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
    fun `loadAds returns a failure when the source payload is malformed`() = runBlocking {
        val result = AdRepository(
            FakeIdealistaApi(failure = SerializationException("malformed payload")),
        ).loadAds()

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

    @Test
    fun `loadDetails maps currency suffix type precedence typed facts and dedicated valid energy ratings`() = runBlocking {
        val result = AdRepository(
            FakeIdealistaApi(
                details = PropertyDetailsDto(
                    adid = 1,
                    price = BigDecimal("1200"),
                    priceInfo = DetailPriceInfoDto(
                        amount = BigDecimal("1200"),
                        currencySuffix = "€",
                    ),
                    propertyType = "homes",
                    extendedPropertyType = "flat",
                    homeType = "penthouse",
                    operation = "sale",
                    multimedia = MultimediaDto(
                        images = listOf(
                            ImageDto("https://images.example/detail-1.jpg", "kitchen"),
                            ImageDto(" ", "bedroom"),
                            ImageDto("https://images.example/detail-2.jpg", "communalareas"),
                        ),
                    ),
                    moreCharacteristics = mapOf(
                        "constructedArea" to JsonPrimitive(133),
                        "roomNumber" to JsonPrimitive(3),
                        "bathNumber" to JsonPrimitive(2),
                        "floor" to JsonPrimitive("2"),
                        "exterior" to JsonPrimitive(false),
                        "lift" to JsonPrimitive(true),
                        "boxroom" to JsonPrimitive(false),
                        "isDuplex" to JsonPrimitive(true),
                        "communityCosts" to JsonPrimitive(330),
                        "energyCertificationType" to JsonPrimitive("g"),
                    ),
                    energyCertification = EnergyCertificationDto(
                        energyConsumption = EnergyGradeDto(type = "a"),
                        emissions = EnergyGradeDto(type = "invalid"),
                    ),
                ),
            ),
        ).loadDetails("selected-listing-42")

        assertEquals(
            PropertyDetails(
                selectedAdId = "selected-listing-42",
                remoteAdId = 1,
                price = BigDecimal("1200"),
                currencySuffix = "€",
                propertyType = "penthouse",
                operation = "sale",
                images = listOf(
                    PropertyImage("https://images.example/detail-1.jpg", PropertyImageTag.KITCHEN),
                    PropertyImage("https://images.example/detail-2.jpg"),
                ),
                constructedAreaSquareMeters = 133,
                rooms = 3,
                bathrooms = 2,
                floor = "2",
                isExterior = false,
                hasLift = true,
                hasStorageRoom = false,
                isDuplex = true,
                communityCosts = BigDecimal("330"),
                energyConsumptionRating = EnergyRating.A,
                energyEmissionsRating = null,
            ),
            result.getOrNull(),
        )
    }
}

private class FakeIdealistaApi(
    private val ads: List<PropertyAdDto> = emptyList(),
    private val details: PropertyDetailsDto? = null,
    private val failure: Throwable? = null,
) : IdealistaApi {

    override suspend fun listAds(): List<PropertyAdDto> {
        failure?.let { throw it }
        return ads
    }

    override suspend fun getDetails(): PropertyDetailsDto {
        return checkNotNull(details) { "Detail endpoint was not configured for this test" }
    }
}
