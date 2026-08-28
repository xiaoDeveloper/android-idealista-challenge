package com.xiao.idealistachallenge.data.repository

import com.xiao.idealistachallenge.data.remote.DetailPriceInfoDto
import com.xiao.idealistachallenge.data.remote.EnergyCertificationDto
import com.xiao.idealistachallenge.data.remote.EnergyGradeDto
import com.xiao.idealistachallenge.data.remote.FeaturesDto
import com.xiao.idealistachallenge.data.remote.ImageDto
import com.xiao.idealistachallenge.data.remote.IdealistaApi
import com.xiao.idealistachallenge.data.remote.MultimediaDto
import com.xiao.idealistachallenge.data.remote.ParkingSpaceDto
import com.xiao.idealistachallenge.data.remote.PriceInfoDto
import com.xiao.idealistachallenge.data.remote.PriceValueDto
import com.xiao.idealistachallenge.data.remote.PropertyAdDto
import com.xiao.idealistachallenge.data.remote.PropertyDetailsDto
import com.xiao.idealistachallenge.model.EnergyRating
import com.xiao.idealistachallenge.model.PropertyAd
import com.xiao.idealistachallenge.model.PropertyDetails
import com.xiao.idealistachallenge.model.PropertyHighlight
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
                        operation = "rent",
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
                    operation = "rent",
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
    fun `loadAds extracts all four highlights in deterministic order`() = runBlocking {
        val result = AdRepository(
            FakeIdealistaApi(
                ads = listOf(
                    PropertyAdDto(
                        propertyCode = "ad-all-highlights",
                        price = BigDecimal("350000.0"),
                        exterior = true,
                        features = FeaturesDto(
                            hasAirConditioning = true,
                            hasBoxRoom = true,
                        ),
                        parkingSpace = ParkingSpaceDto(
                            hasParkingSpace = true,
                            isParkingSpaceIncludedInPrice = true,
                        ),
                    ),
                ),
            ),
        ).loadAds()

        assertTrue(result.isSuccess)
        assertEquals(
            listOf(
                PropertyHighlight.EXTERIOR,
                PropertyHighlight.AIR_CONDITIONING,
                PropertyHighlight.STORAGE_ROOM,
                PropertyHighlight.INCLUDED_PARKING,
            ),
            result.getOrNull()?.single()?.highlights,
        )
    }

    @Test
    fun `loadAds omits highlights when flags are false or null`() = runBlocking {
        val result = AdRepository(
            FakeIdealistaApi(
                ads = listOf(
                    PropertyAdDto(
                        propertyCode = "ad-false",
                        price = BigDecimal("350000.0"),
                        exterior = false,
                        features = FeaturesDto(
                            hasAirConditioning = false,
                            hasBoxRoom = false,
                        ),
                        parkingSpace = ParkingSpaceDto(
                            hasParkingSpace = false,
                            isParkingSpaceIncludedInPrice = false,
                        ),
                    ),
                    PropertyAdDto(
                        propertyCode = "ad-null",
                        price = BigDecimal("350000.0"),
                        exterior = null,
                        features = null,
                        parkingSpace = null,
                    ),
                ),
            ),
        ).loadAds()

        assertTrue(result.isSuccess)
        assertEquals(emptyList<PropertyHighlight>(), result.getOrNull()?.first()?.highlights)
        assertEquals(emptyList<PropertyHighlight>(), result.getOrNull()?.get(1)?.highlights)
    }

    @Test
    fun `loadAds includes parking highlight only when both hasParkingSpace and isParkingSpaceIncludedInPrice are true`() = runBlocking {
        val result = AdRepository(
            FakeIdealistaApi(
                ads = listOf(
                    PropertyAdDto(
                        propertyCode = "parking-both-true",
                        price = BigDecimal("100"),
                        parkingSpace = ParkingSpaceDto(hasParkingSpace = true, isParkingSpaceIncludedInPrice = true),
                    ),
                    PropertyAdDto(
                        propertyCode = "parking-not-included",
                        price = BigDecimal("100"),
                        parkingSpace = ParkingSpaceDto(hasParkingSpace = true, isParkingSpaceIncludedInPrice = false),
                    ),
                    PropertyAdDto(
                        propertyCode = "parking-no-space",
                        price = BigDecimal("100"),
                        parkingSpace = ParkingSpaceDto(hasParkingSpace = false, isParkingSpaceIncludedInPrice = true),
                    ),
                    PropertyAdDto(
                        propertyCode = "parking-included-null",
                        price = BigDecimal("100"),
                        parkingSpace = ParkingSpaceDto(hasParkingSpace = true, isParkingSpaceIncludedInPrice = null),
                    ),
                    PropertyAdDto(
                        propertyCode = "parking-space-null",
                        price = BigDecimal("100"),
                        parkingSpace = ParkingSpaceDto(hasParkingSpace = null, isParkingSpaceIncludedInPrice = true),
                    ),
                ),
            ),
        ).loadAds()

        assertTrue(result.isSuccess)
        val ads = result.getOrThrow()
        assertEquals(listOf(PropertyHighlight.INCLUDED_PARKING), ads[0].highlights)
        assertEquals(emptyList<PropertyHighlight>(), ads[1].highlights)
        assertEquals(emptyList<PropertyHighlight>(), ads[2].highlights)
        assertEquals(emptyList<PropertyHighlight>(), ads[3].highlights)
        assertEquals(emptyList<PropertyHighlight>(), ads[4].highlights)
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
    fun `loadDetails keeps a nonmatching selected listing's core content`() = runBlocking {
        val api = FakeIdealistaApi(
            ads = listOf(
                PropertyAdDto(
                    propertyCode = "2",
                    price = BigDecimal("1200"),
                    priceInfo = PriceInfoDto(PriceValueDto(BigDecimal("1200"), "€/mes")),
                    propertyType = "flat",
                    operation = "rent",
                    address = "Calle de Fortuny",
                    municipality = "Madrid",
                    district = "Almagro",
                    size = BigDecimal("241"),
                    rooms = 4,
                    bathrooms = 4,
                    description = "Listing two description",
                    multimedia = MultimediaDto(
                        listOf(
                            ImageDto("https://images.example/listing-2-a.jpg", "livingRoom"),
                            ImageDto("https://images.example/listing-2-b.jpg", "bedroom"),
                        ),
                    ),
                ),
            ),
            details = detailDto(adid = 1),
        )
        val repository = AdRepository(api)
        repository.loadAds()

        val details = repository.loadDetails("2").getOrThrow()

        assertEquals("2", details.selectedAdId)
        assertEquals(BigDecimal("1200"), details.price)
        assertEquals("€/mes", details.currencySuffix)
        assertEquals("flat", details.propertyType)
        assertEquals("rent", details.operation)
        assertEquals("Calle de Fortuny", details.address)
        assertEquals("Madrid", details.municipality)
        assertEquals("Almagro", details.district)
        assertEquals("Listing two description", details.description)
        assertEquals(241, details.constructedAreaSquareMeters)
        assertEquals(4, details.rooms)
        assertEquals(4, details.bathrooms)
        assertEquals(
            listOf(
                PropertyImage("https://images.example/listing-2-a.jpg", PropertyImageTag.LIVING_ROOM),
                PropertyImage("https://images.example/listing-2-b.jpg", PropertyImageTag.BEDROOM),
            ),
            details.images,
        )
        assertEquals(null, details.floor)
        assertEquals(null, details.energyConsumptionRating)
        assertEquals(null, details.remoteAdId)
        assertEquals(1, api.listRequestCount)
    }

    @Test
    fun `loadDetails enriches only a listing whose identity matches fixed detail`() = runBlocking {
        val repository = AdRepository(
            FakeIdealistaApi(
                ads = listOf(
                    PropertyAdDto(
                        propertyCode = "1",
                        price = BigDecimal("1195000"),
                        propertyType = "flat",
                        size = BigDecimal("133"),
                        rooms = 3,
                        bathrooms = 2,
                        description = "Listing one description",
                        multimedia = MultimediaDto(listOf(ImageDto("https://images.example/listing-1.jpg", "livingRoom"))),
                    ),
                ),
                details = detailDto(adid = 1),
            ),
        )

        val details = repository.loadDetails("1").getOrThrow()

        assertEquals("Listing one description", details.description)
        assertEquals(listOf(PropertyImage("https://images.example/listing-1.jpg", PropertyImageTag.LIVING_ROOM)), details.images)
        assertEquals("2", details.floor)
        assertEquals(true, details.hasLift)
        assertEquals(EnergyRating.A, details.energyConsumptionRating)
        assertEquals(1, details.remoteAdId)
    }

    @Test
    fun `loadDetails refreshes the listing when its snapshot is absent`() = runBlocking {
        val api = FakeIdealistaApi(
            ads = listOf(PropertyAdDto(propertyCode = "2", price = BigDecimal("1200"))),
            details = detailDto(adid = 1),
        )

        val details = AdRepository(api).loadDetails("2").getOrThrow()

        assertEquals(BigDecimal("1200"), details.price)
        assertEquals(1, api.listRequestCount)
    }

    @Test
    fun `loadDetails omits fixed enrichment when that request fails`() = runBlocking {
        val details = AdRepository(
            FakeIdealistaApi(
                ads = listOf(PropertyAdDto(propertyCode = "2", price = BigDecimal("1200"))),
                detailFailure = IOException("detail unavailable"),
            ),
        ).loadDetails("2").getOrThrow()

        assertEquals(BigDecimal("1200"), details.price)
        assertEquals(null, details.remoteAdId)
        assertEquals(null, details.floor)
    }
}

private class FakeIdealistaApi(
    private val ads: List<PropertyAdDto> = emptyList(),
    private val details: PropertyDetailsDto? = null,
    private val failure: Throwable? = null,
    private val detailFailure: Throwable? = null,
) : IdealistaApi {
    var listRequestCount = 0
        private set

    override suspend fun listAds(): List<PropertyAdDto> {
        listRequestCount += 1
        failure?.let { throw it }
        return ads
    }

    override suspend fun getDetails(): PropertyDetailsDto {
        detailFailure?.let { throw it }
        return checkNotNull(details) { "Detail endpoint was not configured for this test" }
    }
}

private fun detailDto(adid: Int): PropertyDetailsDto = PropertyDetailsDto(
    adid = adid,
    price = BigDecimal("1195000"),
    priceInfo = DetailPriceInfoDto(amount = BigDecimal("1195000"), currencySuffix = "€"),
    propertyType = "homes",
    extendedPropertyType = "flat",
    homeType = "penthouse",
    operation = "sale",
    propertyComment = "Fixed property one description",
    multimedia = MultimediaDto(
        images = listOf(
            ImageDto("https://images.example/detail-1.jpg", "kitchen"),
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
    ),
    energyCertification = EnergyCertificationDto(
        energyConsumption = EnergyGradeDto(type = "a"),
        emissions = EnergyGradeDto(type = "invalid"),
    ),
)
