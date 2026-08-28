package com.xiao.idealistachallenge

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.runner.AndroidJUnitRunner
import com.xiao.idealistachallenge.core.AppContainer
import com.xiao.idealistachallenge.data.local.FavoriteDatabase
import com.xiao.idealistachallenge.data.remote.IdealistaApi
import com.xiao.idealistachallenge.data.remote.ImageDto
import com.xiao.idealistachallenge.data.remote.LocationDto
import com.xiao.idealistachallenge.data.remote.MultimediaDto
import com.xiao.idealistachallenge.data.remote.PriceInfoDto
import com.xiao.idealistachallenge.data.remote.PriceValueDto
import com.xiao.idealistachallenge.data.remote.PropertyAdDto
import com.xiao.idealistachallenge.data.remote.PropertyDetailsDto
import com.xiao.idealistachallenge.data.remote.DetailPriceInfoDto
import com.xiao.idealistachallenge.data.remote.EnergyCertificationDto
import com.xiao.idealistachallenge.data.remote.EnergyGradeDto
import java.math.BigDecimal
import kotlinx.serialization.json.JsonPrimitive

class TestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        classLoader: ClassLoader,
        className: String,
        context: Context,
    ): Application = super.newApplication(classLoader, TestApp::class.java.name, context)
}

class TestApp : App() {
    override fun createContainer(): AppContainer = AppContainer(
        context = this,
        idealistaApiOverride = FixtureIdealistaApi,
        favoriteDatabaseOverride = Room.inMemoryDatabaseBuilder(this, FavoriteDatabase::class.java)
            .allowMainThreadQueries()
            .build(),
    )
}

private const val INLINE_IMAGE = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQIHWP4z8DwHwAFgAI/ScLk7wAAAABJRU5ErkJggg=="
private const val FIXTURE_LONG_DESCRIPTION = """
Primera parte de la descripción de prueba con información suficiente para formar una vista previa de varias líneas sin cambiar el texto original.

Segunda parte de la descripción de prueba que conserva este salto de párrafo y añade información suficiente para revelar el contenido completo con una sola acción.

Tercera parte de la descripción de prueba para asegurar que la vista previa de seis líneas se pueda expandir y volver a contraer sin perder contenido.
"""

private object FixtureIdealistaApi : IdealistaApi {
    override suspend fun listAds(): List<PropertyAdDto> = listOf(
        PropertyAdDto(
            propertyCode = "listing-42",
            thumbnail = "https://example.invalid/listing-42-thumbnail.jpg",
            price = BigDecimal("125000"),
            priceInfo = PriceInfoDto(PriceValueDto(BigDecimal("125000"), "€")),
            propertyType = "flat",
            address = "Madrid",
            municipality = "Madrid",
            district = null,
            size = null,
            rooms = null,
            bathrooms = null,
            description = "Fixture listing",
            multimedia = MultimediaDto(
                listOf(
                    ImageDto("https://example.invalid/listing-42-first.jpg", "livingRoom"),
                    ImageDto("https://example.invalid/listing-42-second.jpg", "bedroom"),
                    ImageDto("https://example.invalid/listing-42-third.jpg", "kitchen"),
                ),
            ),
        ),
        PropertyAdDto(
            propertyCode = "listing-single",
            thumbnail = "https://example.invalid/listing-single-thumbnail.jpg",
            price = BigDecimal("126000"),
            priceInfo = PriceInfoDto(PriceValueDto(BigDecimal("126000"), "€")),
            propertyType = "flat",
            address = "Madrid",
            municipality = "Madrid",
            district = null,
            size = null,
            rooms = null,
            bathrooms = null,
            description = "Single image fixture",
            multimedia = MultimediaDto(listOf(ImageDto("https://example.invalid/listing-single.jpg"))),
        ),
        PropertyAdDto(
            propertyCode = "listing-thumbnail-only",
            thumbnail = "https://example.invalid/listing-thumbnail-only.jpg",
            price = BigDecimal("127000"),
            priceInfo = PriceInfoDto(PriceValueDto(BigDecimal("127000"), "€")),
            propertyType = "flat",
            address = "Madrid",
            municipality = "Madrid",
            district = null,
            size = null,
            rooms = null,
            bathrooms = null,
            description = "Thumbnail fixture",
            multimedia = MultimediaDto(emptyList()),
        ),
        PropertyAdDto(
            propertyCode = "listing-empty",
            thumbnail = null,
            price = BigDecimal("128000"),
            priceInfo = PriceInfoDto(PriceValueDto(BigDecimal("128000"), "€")),
            propertyType = "flat",
            address = "Madrid",
            municipality = "Madrid",
            district = null,
            size = null,
            rooms = null,
            bathrooms = null,
            description = "No image fixture",
            multimedia = MultimediaDto(emptyList()),
        ),
        PropertyAdDto(
            propertyCode = "listing-broken-image",
            thumbnail = null,
            price = BigDecimal("129000"),
            priceInfo = PriceInfoDto(PriceValueDto(BigDecimal("129000"), "€")),
            propertyType = "flat",
            address = "Madrid",
            municipality = "Madrid",
            district = null,
            size = null,
            rooms = null,
            bathrooms = null,
            description = "Broken image fixture",
            multimedia = MultimediaDto(
                listOf(
                    ImageDto("https://example.invalid/listing-broken-image-first.jpg"),
                    ImageDto("not-a-valid-url"),
                    ImageDto("https://example.invalid/listing-broken-image-third.jpg"),
                ),
            ),
        ),
    )

    override suspend fun getDetails(): PropertyDetailsDto = PropertyDetailsDto(
        adid = 1,
        price = BigDecimal("125000"),
        priceInfo = DetailPriceInfoDto(BigDecimal("125000"), "€"),
        propertyType = "homes",
        extendedPropertyType = "flat",
        homeType = "flat",
        operation = "sale",
        propertyComment = FIXTURE_LONG_DESCRIPTION,
        multimedia = MultimediaDto(
            listOf(
                ImageDto(INLINE_IMAGE, "livingRoom"),
                ImageDto(INLINE_IMAGE, "communalareas"),
                ImageDto(INLINE_IMAGE),
                ImageDto(INLINE_IMAGE, "bedroom"),
            ),
        ),
        ubication = LocationDto(latitude = BigDecimal("40.4363"), longitude = BigDecimal("-3.6834")),
        moreCharacteristics = mapOf(
            "constructedArea" to JsonPrimitive(133),
            "roomNumber" to JsonPrimitive(3),
            "bathNumber" to JsonPrimitive(2),
            "floor" to JsonPrimitive("2"),
            "exterior" to JsonPrimitive(false),
            "lift" to JsonPrimitive(true),
            "boxroom" to JsonPrimitive(false),
            "isDuplex" to JsonPrimitive(false),
            "communityCosts" to JsonPrimitive(330),
            "housingFurnitures" to JsonPrimitive("unknown"),
        ),
        energyCertification = EnergyCertificationDto(
            energyConsumption = EnergyGradeDto("e"),
            emissions = EnergyGradeDto("e"),
        ),
    )
}
