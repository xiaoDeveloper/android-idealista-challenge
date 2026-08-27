package com.xiao.idealistachallenge

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.runner.AndroidJUnitRunner
import com.xiao.idealistachallenge.core.AppContainer
import com.xiao.idealistachallenge.data.local.FavoriteDatabase
import com.xiao.idealistachallenge.data.remote.IdealistaApi
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
            thumbnail = null,
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
            multimedia = MultimediaDto(emptyList()),
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
