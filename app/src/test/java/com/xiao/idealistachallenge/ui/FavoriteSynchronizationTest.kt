package com.xiao.idealistachallenge.ui

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.xiao.idealistachallenge.data.local.FavoriteDatabase
import com.xiao.idealistachallenge.data.remote.IdealistaApi
import com.xiao.idealistachallenge.data.remote.LocationDto
import com.xiao.idealistachallenge.data.remote.MultimediaDto
import com.xiao.idealistachallenge.data.remote.PriceInfoDto
import com.xiao.idealistachallenge.data.remote.PriceValueDto
import com.xiao.idealistachallenge.data.remote.PropertyAdDto
import com.xiao.idealistachallenge.data.remote.PropertyDetailsDto
import com.xiao.idealistachallenge.data.repository.AdRepository
import com.xiao.idealistachallenge.data.repository.FavoriteRepository
import com.xiao.idealistachallenge.ui.detail.DetailUiState
import com.xiao.idealistachallenge.ui.detail.DetailViewModel
import com.xiao.idealistachallenge.ui.listing.ListingUiState
import com.xiao.idealistachallenge.ui.listing.ListingViewModel
import java.math.BigDecimal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FavoriteSynchronizationTest {

    private lateinit var database: FavoriteDatabase
    private lateinit var favoriteRepository: FavoriteRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            FavoriteDatabase::class.java,
        ).allowMainThreadQueries().build()
        favoriteRepository = FavoriteRepository(database.favoriteDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `listing and recreated detail share the selected property Room favorite record`() = runBlocking {
        val repository = AdRepository(FavoriteApi())
        var nextListingFavoriteTime = 1_000L
        val listing = ListingViewModel(
            adRepository = repository,
            favoriteRepository = favoriteRepository,
            dispatcher = Dispatchers.Unconfined,
            nowEpochMillis = {
                nextListingFavoriteTime.also { nextListingFavoriteTime = 3_000L }
            },
        )
        listing.load()
        listing.uiState.awaitListingFavorite(null)

        listing.toggleFavorite(AD_ID, favoritedAtEpochMillis = null)
        listing.uiState.awaitListingFavorite(1_000L)

        val detail = newDetailViewModel(repository, nowEpochMillis = { 2_000L })
        detail.load()
        detail.uiState.awaitDetailFavorite(1_000L)

        detail.toggleFavorite()
        detail.uiState.awaitDetailFavorite(null)
        listing.uiState.awaitListingFavorite(null)

        listing.toggleFavorite(AD_ID, favoritedAtEpochMillis = null)
        listing.uiState.awaitListingFavorite(3_000L)

        val recreatedDetail = newDetailViewModel(repository, nowEpochMillis = { 3_000L })
        recreatedDetail.load()
        recreatedDetail.uiState.awaitDetailFavorite(3_000L)
        Unit
    }

    private fun newDetailViewModel(
        repository: AdRepository,
        nowEpochMillis: () -> Long,
    ) = DetailViewModel(
        adRepository = repository,
        favoriteRepository = favoriteRepository,
        selectedAdId = AD_ID,
        dispatcher = Dispatchers.Unconfined,
        nowEpochMillis = nowEpochMillis,
    )

    private suspend fun kotlinx.coroutines.flow.StateFlow<ListingUiState>.awaitListingFavorite(
        expectedTimestamp: Long?,
    ) = first { state ->
        state is ListingUiState.Content &&
            state.rows.single().favoritedAtEpochMillis == expectedTimestamp
    }.also { state ->
        assertEquals(expectedTimestamp, (state as ListingUiState.Content).rows.single().favoritedAtEpochMillis)
    }

    private suspend fun kotlinx.coroutines.flow.StateFlow<DetailUiState>.awaitDetailFavorite(
        expectedTimestamp: Long?,
    ) = first { state ->
        state is DetailUiState.Content && state.favoritedAtEpochMillis == expectedTimestamp
    }.also { state ->
        assertEquals(expectedTimestamp, (state as DetailUiState.Content).favoritedAtEpochMillis)
    }

    private companion object {
        const val AD_ID = "listing-42"
    }
}

private class FavoriteApi : IdealistaApi {
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
            description = null,
            multimedia = MultimediaDto(emptyList()),
        ),
    )

    override suspend fun getDetails(): PropertyDetailsDto = PropertyDetailsDto(
        adid = 1,
        price = BigDecimal("125000"),
        propertyComment = null,
        multimedia = MultimediaDto(emptyList()),
        ubication = LocationDto(latitude = null, longitude = null),
        moreCharacteristics = emptyMap(),
    )
}
