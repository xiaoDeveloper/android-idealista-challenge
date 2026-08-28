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
import java.math.BigDecimal

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
        propertyComment = "Fixture detail",
        multimedia = MultimediaDto(emptyList()),
        ubication = LocationDto(latitude = null, longitude = null),
        moreCharacteristics = emptyMap(),
    )
}
