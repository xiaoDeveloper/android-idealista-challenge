package com.xiao.idealistachallenge.ui.listing

import com.xiao.idealistachallenge.R
import com.xiao.idealistachallenge.core.ErrorMessageMapper
import com.xiao.idealistachallenge.data.local.FavoriteDao
import com.xiao.idealistachallenge.data.local.FavoriteEntity
import com.xiao.idealistachallenge.data.remote.IdealistaApi
import com.xiao.idealistachallenge.data.remote.ImageDto
import com.xiao.idealistachallenge.data.remote.MultimediaDto
import com.xiao.idealistachallenge.data.remote.PriceInfoDto
import com.xiao.idealistachallenge.data.remote.PropertyAdDto
import com.xiao.idealistachallenge.data.remote.PropertyDetailsDto
import com.xiao.idealistachallenge.data.repository.AdRepository
import com.xiao.idealistachallenge.data.repository.FavoriteRepository
import com.xiao.idealistachallenge.model.PropertyAd
import java.io.IOException
import java.math.BigDecimal
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * T012 defines the public contract expected by T014.
 *
 * The ViewModel is constructed with a coroutine dispatcher so these tests can use
 * Dispatchers.Unconfined without kotlinx-coroutines-test or timing-based sleeps.
 * Production code should use its lifecycle scope and an IO-capable dispatcher.
 */
class ListingViewModelTest {

    @Test
    fun `initial state stays loading until the listing request completes`() = runBlocking {
        val requestStarted = CompletableDeferred<Unit>()
        val response = CompletableDeferred<List<PropertyAdDto>>()
        val viewModel = newViewModel(
            BlockingIdealistaApi(
                requestStarted = requestStarted,
                response = response,
            ),
        )

        try {
            assertEquals(ListingUiState.Loading, viewModel.uiState.value)

            viewModel.load()
            requestStarted.await()
            assertEquals(ListingUiState.Loading, viewModel.uiState.value)

            response.complete(listOf(adDto("ad-1", BigDecimal("125000"))))
            val content = viewModel.uiState.first { it is ListingUiState.Content }

            assertEquals(1, (content as ListingUiState.Content).rows.size)
        } finally {
            viewModel.clear()
        }
    }

    @Test
    fun `successful response becomes immutable content rows`() = runBlocking {
        val firstAd = propertyAd("ad-1", BigDecimal("125000"))
        val secondAd = propertyAd("ad-2", BigDecimal("245000"))
        val viewModel = newViewModel(
            FakeIdealistaApi(
                responses = listOf(
                    Result.success(listOf(adDto(firstAd.propertyCode, firstAd.price))),
                    Result.success(listOf(adDto(secondAd.propertyCode, secondAd.price))),
                ),
            ),
        )

        try {
            viewModel.load()
            val content = viewModel.awaitContent()

            assertEquals(
                listOf(
                    ListingRowUiModel(ad = firstAd, favoritedAtEpochMillis = null),
                    ListingRowUiModel(ad = secondAd, favoritedAtEpochMillis = null),
                ),
                content.rows,
            )
            assertEquals(listOf(firstAd.propertyCode, secondAd.propertyCode), content.rows.map { it.ad.propertyCode })
        } finally {
            viewModel.clear()
        }
    }

    @Test
    fun `empty response becomes the empty state`() = runBlocking {
        val viewModel = newViewModel(
            FakeIdealistaApi(responses = listOf(Result.success(emptyList()))),
        )

        try {
            viewModel.load()

            assertEquals(ListingUiState.Empty, viewModel.awaitState { it is ListingUiState.Empty })
        } finally {
            viewModel.clear()
        }
    }

    @Test
    fun `listing failure exposes stable friendly copy instead of the raw exception`() = runBlocking {
        val rawFailureMessage = "raw backend token and stack details"
        val viewModel = newViewModel(
            FakeIdealistaApi(
                responses = listOf(Result.failure(IOException(rawFailureMessage))),
            ),
        )

        try {
            viewModel.load()
            val error = viewModel.awaitState { it is ListingUiState.Error } as ListingUiState.Error

            assertEquals(
                ErrorMessageMapper.forListing(IOException(rawFailureMessage)),
                error.userFacingError,
            )
            assertEquals(R.string.error_list_message, error.userFacingError.messageResId)
            assertFalse(error.toString().contains(rawFailureMessage))
        } finally {
            viewModel.clear()
        }
    }

    @Test
    fun `retry after a failure reaches content`() = runBlocking {
        val recoveredAd = propertyAd("ad-recovered", BigDecimal("310000"))
        val viewModel = newViewModel(
            FakeIdealistaApi(
                responses = listOf(
                    Result.failure(IOException("temporary network failure")),
                    Result.success(listOf(adDto(recoveredAd.propertyCode, recoveredAd.price))),
                ),
            ),
        )

        try {
            viewModel.load()
            assertTrue(viewModel.awaitState { it is ListingUiState.Error } is ListingUiState.Error)

            viewModel.retry()
            val content = viewModel.awaitContent()

            assertEquals(recoveredAd.propertyCode, content.rows.single().ad.propertyCode)
        } finally {
            viewModel.clear()
        }
    }

    @Test
    fun `favorite creation timestamp is projected onto the matching row`() = runBlocking {
        val matchingAd = propertyAd("ad-favorite", BigDecimal("415000"))
        val otherAd = propertyAd("ad-other", BigDecimal("425000"))
        val favoriteRepository = FavoriteRepository(InMemoryFavoriteDao())
        val favoritedAt = 1_756_000_000_000L
        favoriteRepository.favorite(matchingAd.propertyCode, favoritedAt)
        val viewModel = newViewModel(
            FakeIdealistaApi(
                responses = listOf(
                    Result.success(
                        listOf(
                            adDto(matchingAd.propertyCode, matchingAd.price),
                            adDto(otherAd.propertyCode, otherAd.price),
                        ),
                    ),
                ),
            ),
            favoriteRepository = favoriteRepository,
        )

        try {
            viewModel.load()
            val content = viewModel.awaitContent()

            assertEquals(
                favoritedAt,
                content.rows.first { it.ad.propertyCode == matchingAd.propertyCode }
                    .favoritedAtEpochMillis,
            )
            assertEquals(
                null,
                content.rows.first { it.ad.propertyCode == otherAd.propertyCode }
                    .favoritedAtEpochMillis,
            )
        } finally {
            viewModel.clear()
        }
    }

    private fun newViewModel(
        api: IdealistaApi,
        favoriteRepository: FavoriteRepository = FavoriteRepository(InMemoryFavoriteDao()),
    ): ListingViewModel = ListingViewModel(
        adRepository = AdRepository(api),
        favoriteRepository = favoriteRepository,
        dispatcher = Dispatchers.Unconfined,
    )

    private suspend fun ListingViewModel.awaitContent(): ListingUiState.Content =
        awaitState { it is ListingUiState.Content } as ListingUiState.Content

    private suspend fun ListingViewModel.awaitState(
        predicate: (ListingUiState) -> Boolean,
    ): ListingUiState = uiState.first(predicate)
}

private class BlockingIdealistaApi(
    private val requestStarted: CompletableDeferred<Unit>,
    private val response: CompletableDeferred<List<PropertyAdDto>>,
) : IdealistaApi {

    override suspend fun listAds(): List<PropertyAdDto> {
        requestStarted.complete(Unit)
        return response.await()
    }

    override suspend fun getDetails(): PropertyDetailsDto {
        throw UnsupportedOperationException("Detail endpoint is not used by listing tests")
    }
}

private class FakeIdealistaApi(
    responses: List<Result<List<PropertyAdDto>>>,
) : IdealistaApi {

    private val responses = ArrayDeque(responses)

    override suspend fun listAds(): List<PropertyAdDto> = responses.removeFirst().getOrThrow()

    override suspend fun getDetails(): PropertyDetailsDto {
        throw UnsupportedOperationException("Detail endpoint is not used by listing tests")
    }
}

private class InMemoryFavoriteDao : FavoriteDao {
    private val favorites = MutableStateFlow<Map<String, FavoriteEntity>>(emptyMap())

    override fun observeFavorite(adId: String): Flow<FavoriteEntity?> =
        favorites.map { it[adId] }.distinctUntilChanged()

    override suspend fun upsert(favorite: FavoriteEntity) {
        favorites.update { it + (favorite.adId to favorite) }
    }

    override suspend fun deleteByAdId(adId: String) {
        favorites.update { it - adId }
    }
}

private fun propertyAd(propertyCode: String, price: BigDecimal): PropertyAd = PropertyAd(
    propertyCode = propertyCode,
    thumbnailUrl = "https://images.example/$propertyCode-thumbnail.jpg",
    price = price,
    currencySuffix = "€",
    propertyType = "flat",
    address = "Calle Example, 1",
    municipality = "Madrid",
    district = "Centro",
    sizeSquareMeters = 80,
    rooms = 3,
    bathrooms = 2,
    description = "A bright home",
    imageUrls = listOf("https://images.example/$propertyCode.jpg"),
)

private fun adDto(propertyCode: String, price: BigDecimal): PropertyAdDto = PropertyAdDto(
    propertyCode = propertyCode,
    thumbnail = "https://images.example/$propertyCode-thumbnail.jpg",
    price = price,
    priceInfo = PriceInfoDto(currencySuffix = "€"),
    propertyType = "flat",
    address = "Calle Example, 1",
    municipality = "Madrid",
    district = "Centro",
    size = 80,
    rooms = 3,
    bathrooms = 2,
    description = "A bright home",
    multimedia = MultimediaDto(
        images = listOf(ImageDto(url = "https://images.example/$propertyCode.jpg")),
    ),
)
