package com.xiao.idealistachallenge.ui.listing

import com.xiao.idealistachallenge.R
import com.xiao.idealistachallenge.core.ErrorMessageMapper
import com.xiao.idealistachallenge.data.local.FavoriteDao
import com.xiao.idealistachallenge.data.local.FavoriteEntity
import com.xiao.idealistachallenge.data.remote.IdealistaApi
import com.xiao.idealistachallenge.data.remote.ImageDto
import com.xiao.idealistachallenge.data.remote.MultimediaDto
import com.xiao.idealistachallenge.data.remote.PriceInfoDto
import com.xiao.idealistachallenge.data.remote.PriceValueDto
import com.xiao.idealistachallenge.data.remote.PropertyAdDto
import com.xiao.idealistachallenge.data.remote.PropertyDetailsDto
import com.xiao.idealistachallenge.data.repository.AdRepository
import com.xiao.idealistachallenge.data.repository.FavoriteRepository
import com.xiao.idealistachallenge.model.PropertyAd
import com.xiao.idealistachallenge.model.PropertyImage
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

        assertEquals(ListingUiState.Loading, viewModel.uiState.value)

        viewModel.load()
        requestStarted.await()
        assertEquals(ListingUiState.Loading, viewModel.uiState.value)

        response.complete(listOf(adDto("ad-1", BigDecimal("125000"))))
        val content = viewModel.uiState.first { it is ListingUiState.Content }

        assertEquals(1, (content as ListingUiState.Content).rows.size)
    }

    @Test
    fun `successful response becomes immutable content rows`() = runBlocking {
        val firstAd = propertyAd("ad-1", BigDecimal("125000"))
        val secondAd = propertyAd("ad-2", BigDecimal("245000"))
        val viewModel = newViewModel(
            FakeIdealistaApi(
                responses = listOf(
                    Result.success(
                        listOf(
                            adDto(firstAd.propertyCode, firstAd.price),
                            adDto(secondAd.propertyCode, secondAd.price),
                        ),
                    ),
                ),
            ),
        )

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
    }

    @Test
    fun `empty response becomes the empty state`() = runBlocking {
        val viewModel = newViewModel(
            FakeIdealistaApi(responses = listOf(Result.success(emptyList()))),
        )

        viewModel.load()

        assertEquals(ListingUiState.Empty, viewModel.awaitState { it is ListingUiState.Empty })
    }

    @Test
    fun `listing failure exposes stable friendly copy instead of the raw exception`() = runBlocking {
        val rawFailureMessage = "raw backend token and stack details"
        val viewModel = newViewModel(
            FakeIdealistaApi(
                responses = listOf(Result.failure(IOException(rawFailureMessage))),
            ),
        )

        viewModel.load()
        val error = viewModel.awaitState { it is ListingUiState.Error } as ListingUiState.Error

        assertEquals(
            ErrorMessageMapper.forListing(IOException(rawFailureMessage)),
            error.userFacingError,
        )
        assertEquals(R.string.error_list_message, error.userFacingError.messageResId)
        assertFalse(error.toString().contains(rawFailureMessage))
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

        viewModel.load()
        assertTrue(viewModel.awaitState { it is ListingUiState.Error } is ListingUiState.Error)

        viewModel.retry()
        val content = viewModel.awaitContent()

        assertEquals(recoveredAd.propertyCode, content.rows.single().ad.propertyCode)
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
    }

    @Test
    fun `favorite actions update the Room-backed row state without reloading listings`() = runBlocking {
        val favoriteRepository = FavoriteRepository(InMemoryFavoriteDao())
        val viewModel = newViewModel(
            FakeIdealistaApi(responses = listOf(Result.success(listOf(adDto("ad-favorite", BigDecimal("415000"))))),
            ),
            favoriteRepository = favoriteRepository,
            nowEpochMillis = { 2_000L },
        )

        viewModel.load()
        assertEquals(null, viewModel.awaitContent().rows.single().favoritedAtEpochMillis)

        viewModel.toggleFavorite(adId = "ad-favorite", favoritedAtEpochMillis = null)
        assertEquals(2_000L, viewModel.awaitContent { it.rows.single().favoritedAtEpochMillis == 2_000L }
            .rows.single().favoritedAtEpochMillis)

        viewModel.toggleFavorite(adId = "ad-favorite", favoritedAtEpochMillis = 2_000L)
        assertEquals(null, viewModel.awaitContent { it.rows.single().favoritedAtEpochMillis == null }
            .rows.single().favoritedAtEpochMillis)
    }

    @Test
    fun `initial discovery state defaults to ALL with no price sort direction in source order`() = runBlocking {
        val firstAd = propertyAd("ad-1", BigDecimal("200000"), operation = "sale")
        val secondAd = propertyAd("ad-2", BigDecimal("100000"), operation = "rent")
        val viewModel = newViewModel(
            FakeIdealistaApi(
                responses = listOf(
                    Result.success(
                        listOf(
                            adDto(firstAd.propertyCode, firstAd.price, firstAd.operation),
                            adDto(secondAd.propertyCode, secondAd.price, secondAd.operation),
                        ),
                    ),
                ),
            ),
        )

        viewModel.load()
        val content = viewModel.awaitContent()

        assertEquals(ListingDiscoveryUiState(category = ListingCategory.ALL, priceSortDirection = null), content.discovery)
        assertEquals(listOf("ad-1", "ad-2"), content.rows.map { it.ad.propertyCode })
    }

    @Test
    fun `filtering by SALE or RENT matches normalized trimmed operations in original relative order`() = runBlocking {
        val saleAd1 = propertyAd("sale-1", BigDecimal("300000"), operation = "sale")
        val rentAd1 = propertyAd("rent-1", BigDecimal("1200"), operation = " rent ")
        val saleAd2 = propertyAd("sale-2", BigDecimal("200000"), operation = "SALE")
        val rentAd2 = propertyAd("rent-2", BigDecimal("1500"), operation = "Rent")
        val viewModel = newViewModel(
            FakeIdealistaApi(
                responses = listOf(
                    Result.success(
                        listOf(
                            adDto(saleAd1.propertyCode, saleAd1.price, saleAd1.operation),
                            adDto(rentAd1.propertyCode, rentAd1.price, rentAd1.operation),
                            adDto(saleAd2.propertyCode, saleAd2.price, saleAd2.operation),
                            adDto(rentAd2.propertyCode, rentAd2.price, rentAd2.operation),
                        ),
                    ),
                ),
            ),
        )

        viewModel.load()
        viewModel.awaitContent()

        viewModel.selectCategory(ListingCategory.SALE)
        val saleContent = viewModel.awaitContent { it.discovery.category == ListingCategory.SALE }
        assertEquals(listOf("sale-1", "sale-2"), saleContent.rows.map { it.ad.propertyCode })

        viewModel.selectCategory(ListingCategory.RENT)
        val rentContent = viewModel.awaitContent { it.discovery.category == ListingCategory.RENT }
        assertEquals(listOf("rent-1", "rent-2"), rentContent.rows.map { it.ad.propertyCode })
    }

    @Test
    fun `unsupported or missing operations appear only in ALL and are excluded from SALE and RENT`() = runBlocking {
        val unsupportedAd = propertyAd("other-1", BigDecimal("150000"), operation = "unknown")
        val missingOpAd = propertyAd("other-2", BigDecimal("160000"), operation = null)
        val blankOpAd = propertyAd("other-3", BigDecimal("170000"), operation = "   ")
        val saleAd = propertyAd("sale-1", BigDecimal("250000"), operation = "sale")
        val rentAd = propertyAd("rent-1", BigDecimal("1000"), operation = "rent")
        val viewModel = newViewModel(
            FakeIdealistaApi(
                responses = listOf(
                    Result.success(
                        listOf(
                            adDto(unsupportedAd.propertyCode, unsupportedAd.price, unsupportedAd.operation),
                            adDto(missingOpAd.propertyCode, missingOpAd.price, missingOpAd.operation),
                            adDto(blankOpAd.propertyCode, blankOpAd.price, blankOpAd.operation),
                            adDto(saleAd.propertyCode, saleAd.price, saleAd.operation),
                            adDto(rentAd.propertyCode, rentAd.price, rentAd.operation),
                        ),
                    ),
                ),
            ),
        )

        viewModel.load()
        val allContent = viewModel.awaitContent()
        assertEquals(5, allContent.rows.size)

        viewModel.selectCategory(ListingCategory.SALE)
        val saleContent = viewModel.awaitContent { it.discovery.category == ListingCategory.SALE }
        assertEquals(listOf("sale-1"), saleContent.rows.map { it.ad.propertyCode })

        viewModel.selectCategory(ListingCategory.RENT)
        val rentContent = viewModel.awaitContent { it.discovery.category == ListingCategory.RENT }
        assertEquals(listOf("rent-1"), rentContent.rows.map { it.ad.propertyCode })
    }

    @Test
    fun `selecting price sort direction ascending or descending stably orders prices within category`() = runBlocking {
        val sale1 = propertyAd("sale-mid-1", BigDecimal("200000"), operation = "sale")
        val sale2 = propertyAd("sale-low", BigDecimal("100000"), operation = "sale")
        val sale3 = propertyAd("sale-mid-2", BigDecimal("200000"), operation = "sale")
        val sale4 = propertyAd("sale-high", BigDecimal("400000"), operation = "sale")
        val viewModel = newViewModel(
            FakeIdealistaApi(
                responses = listOf(
                    Result.success(
                        listOf(
                            adDto(sale1.propertyCode, sale1.price, sale1.operation),
                            adDto(sale2.propertyCode, sale2.price, sale2.operation),
                            adDto(sale3.propertyCode, sale3.price, sale3.operation),
                            adDto(sale4.propertyCode, sale4.price, sale4.operation),
                        ),
                    ),
                ),
            ),
        )

        viewModel.load()
        viewModel.selectCategory(ListingCategory.SALE)

        viewModel.selectPriceSortDirection(PriceSortDirection.ASCENDING)
        val ascendingContent = viewModel.awaitContent { it.discovery.priceSortDirection == PriceSortDirection.ASCENDING }
        assertEquals(
            listOf("sale-low", "sale-mid-1", "sale-mid-2", "sale-high"),
            ascendingContent.rows.map { it.ad.propertyCode },
        )

        viewModel.selectPriceSortDirection(PriceSortDirection.DESCENDING)
        val descendingContent = viewModel.awaitContent { it.discovery.priceSortDirection == PriceSortDirection.DESCENDING }
        assertEquals(
            listOf("sale-high", "sale-mid-1", "sale-mid-2", "sale-low"),
            descendingContent.rows.map { it.ad.propertyCode },
        )
    }

    @Test
    fun `switching between SALE and RENT preserves active price sort direction`() = runBlocking {
        val sale1 = propertyAd("sale-1", BigDecimal("200000"), operation = "sale")
        val sale2 = propertyAd("sale-2", BigDecimal("100000"), operation = "sale")
        val rent1 = propertyAd("rent-1", BigDecimal("2000"), operation = "rent")
        val rent2 = propertyAd("rent-2", BigDecimal("1000"), operation = "rent")
        val viewModel = newViewModel(
            FakeIdealistaApi(
                responses = listOf(
                    Result.success(
                        listOf(
                            adDto(sale1.propertyCode, sale1.price, sale1.operation),
                            adDto(sale2.propertyCode, sale2.price, sale2.operation),
                            adDto(rent1.propertyCode, rent1.price, rent1.operation),
                            adDto(rent2.propertyCode, rent2.price, rent2.operation),
                        ),
                    ),
                ),
            ),
        )

        viewModel.load()
        viewModel.selectCategory(ListingCategory.SALE)
        viewModel.selectPriceSortDirection(PriceSortDirection.ASCENDING)
        val saleContent = viewModel.awaitContent { it.discovery.priceSortDirection == PriceSortDirection.ASCENDING }
        assertEquals(listOf("sale-2", "sale-1"), saleContent.rows.map { it.ad.propertyCode })

        viewModel.selectCategory(ListingCategory.RENT)
        val rentContent = viewModel.awaitContent { it.discovery.category == ListingCategory.RENT }
        assertEquals(PriceSortDirection.ASCENDING, rentContent.discovery.priceSortDirection)
        assertEquals(listOf("rent-2", "rent-1"), rentContent.rows.map { it.ad.propertyCode })
    }

    @Test
    fun `switching back to ALL clears price sort direction and restores original source order`() = runBlocking {
        val sale1 = propertyAd("sale-1", BigDecimal("200000"), operation = "sale")
        val sale2 = propertyAd("sale-2", BigDecimal("100000"), operation = "sale")
        val rent1 = propertyAd("rent-1", BigDecimal("1500"), operation = "rent")
        val viewModel = newViewModel(
            FakeIdealistaApi(
                responses = listOf(
                    Result.success(
                        listOf(
                            adDto(sale1.propertyCode, sale1.price, sale1.operation),
                            adDto(sale2.propertyCode, sale2.price, sale2.operation),
                            adDto(rent1.propertyCode, rent1.price, rent1.operation),
                        ),
                    ),
                ),
            ),
        )

        viewModel.load()
        viewModel.selectCategory(ListingCategory.SALE)
        viewModel.selectPriceSortDirection(PriceSortDirection.ASCENDING)
        viewModel.awaitContent { it.discovery.priceSortDirection == PriceSortDirection.ASCENDING }

        viewModel.selectCategory(ListingCategory.ALL)
        val allContent = viewModel.awaitContent { it.discovery.category == ListingCategory.ALL }
        assertEquals(null, allContent.discovery.priceSortDirection)
        assertEquals(listOf("sale-1", "sale-2", "rent-1"), allContent.rows.map { it.ad.propertyCode })
    }

    @Test
    fun `selecting price sort direction while ALL is active is ignored`() = runBlocking {
        val sale1 = propertyAd("sale-1", BigDecimal("200000"), operation = "sale")
        val sale2 = propertyAd("sale-2", BigDecimal("100000"), operation = "sale")
        val viewModel = newViewModel(
            FakeIdealistaApi(
                responses = listOf(
                    Result.success(
                        listOf(
                            adDto(sale1.propertyCode, sale1.price, sale1.operation),
                            adDto(sale2.propertyCode, sale2.price, sale2.operation),
                        ),
                    ),
                ),
            ),
        )

        viewModel.load()
        viewModel.awaitContent()

        viewModel.selectPriceSortDirection(PriceSortDirection.ASCENDING)
        val content = viewModel.awaitContent()
        assertEquals(ListingCategory.ALL, content.discovery.category)
        assertEquals(null, content.discovery.priceSortDirection)
        assertEquals(listOf("sale-1", "sale-2"), content.rows.map { it.ad.propertyCode })
    }

    @Test
    fun `discovery changes transform local state without additional adRepository requests`() = runBlocking {
        var apiCalls = 0
        val countingApi = object : IdealistaApi {
            override suspend fun listAds(): List<PropertyAdDto> {
                apiCalls++
                return listOf(
                    adDto("sale-1", BigDecimal("200000"), "sale"),
                    adDto("rent-1", BigDecimal("1500"), "rent"),
                )
            }

            override suspend fun getDetails(): PropertyDetailsDto {
                throw UnsupportedOperationException()
            }
        }
        val viewModel = newViewModel(countingApi)

        viewModel.load()
        viewModel.awaitContent()
        assertEquals(1, apiCalls)

        viewModel.selectCategory(ListingCategory.SALE)
        viewModel.awaitContent { it.discovery.category == ListingCategory.SALE }
        assertEquals(1, apiCalls)

        viewModel.selectPriceSortDirection(PriceSortDirection.DESCENDING)
        viewModel.awaitContent { it.discovery.priceSortDirection == PriceSortDirection.DESCENDING }
        assertEquals(1, apiCalls)

        viewModel.selectCategory(ListingCategory.RENT)
        viewModel.awaitContent { it.discovery.category == ListingCategory.RENT }
        assertEquals(1, apiCalls)

        viewModel.selectCategory(ListingCategory.ALL)
        viewModel.awaitContent { it.discovery.category == ListingCategory.ALL }
        assertEquals(1, apiCalls)
    }

    @Test
    fun `filtered category with zero matches emits content with empty rows instead of empty state`() = runBlocking {
        val saleAd = propertyAd("sale-1", BigDecimal("200000"), operation = "sale")
        val viewModel = newViewModel(
            FakeIdealistaApi(
                responses = listOf(
                    Result.success(listOf(adDto(saleAd.propertyCode, saleAd.price, saleAd.operation))),
                ),
            ),
        )

        viewModel.load()
        viewModel.awaitContent()

        viewModel.selectCategory(ListingCategory.RENT)
        val rentContent = viewModel.awaitContent { it.discovery.category == ListingCategory.RENT }
        assertEquals(0, rentContent.rows.size)
        assertEquals(ListingCategory.RENT, rentContent.discovery.category)
        assertFalse(viewModel.uiState.value is ListingUiState.Empty)
    }

    @Test
    fun `favorite updates preserve active category filtering and price sorting`() = runBlocking {
        val favoriteRepository = FavoriteRepository(InMemoryFavoriteDao())
        val sale1 = propertyAd("sale-1", BigDecimal("300000"), operation = "sale")
        val sale2 = propertyAd("sale-2", BigDecimal("100000"), operation = "sale")
        val rent1 = propertyAd("rent-1", BigDecimal("1000"), operation = "rent")
        val viewModel = newViewModel(
            FakeIdealistaApi(
                responses = listOf(
                    Result.success(
                        listOf(
                            adDto(sale1.propertyCode, sale1.price, sale1.operation),
                            adDto(sale2.propertyCode, sale2.price, sale2.operation),
                            adDto(rent1.propertyCode, rent1.price, rent1.operation),
                        ),
                    ),
                ),
            ),
            favoriteRepository = favoriteRepository,
            nowEpochMillis = { 5_000L },
        )

        viewModel.load()
        viewModel.selectCategory(ListingCategory.SALE)
        viewModel.selectPriceSortDirection(PriceSortDirection.ASCENDING)
        viewModel.awaitContent { it.discovery.priceSortDirection == PriceSortDirection.ASCENDING }

        viewModel.toggleFavorite(adId = "sale-2", favoritedAtEpochMillis = null)
        val updatedContent = viewModel.awaitContent {
            it.rows.firstOrNull { row -> row.ad.propertyCode == "sale-2" }?.favoritedAtEpochMillis == 5_000L
        }

        assertEquals(listOf("sale-2", "sale-1"), updatedContent.rows.map { it.ad.propertyCode })
        assertEquals(5_000L, updatedContent.rows.first { it.ad.propertyCode == "sale-2" }.favoritedAtEpochMillis)
        assertEquals(ListingCategory.SALE, updatedContent.discovery.category)
        assertEquals(PriceSortDirection.ASCENDING, updatedContent.discovery.priceSortDirection)
    }

    @Test
    fun `retry reapplies current discovery state to newly loaded ads`() = runBlocking {
        val viewModel = newViewModel(
            FakeIdealistaApi(
                responses = listOf(
                    Result.failure(IOException("temporary error")),
                    Result.success(
                        listOf(
                            adDto("sale-1", BigDecimal("300000"), "sale"),
                            adDto("sale-2", BigDecimal("100000"), "sale"),
                            adDto("rent-1", BigDecimal("1000"), "rent"),
                        ),
                    ),
                ),
            ),
        )

        viewModel.load()
        viewModel.awaitState { it is ListingUiState.Error }

        viewModel.selectCategory(ListingCategory.SALE)
        viewModel.selectPriceSortDirection(PriceSortDirection.ASCENDING)

        viewModel.retry()
        val content = viewModel.awaitContent { it.discovery.priceSortDirection == PriceSortDirection.ASCENDING }
        assertEquals(listOf("sale-2", "sale-1"), content.rows.map { it.ad.propertyCode })
        assertEquals(ListingCategory.SALE, content.discovery.category)
        assertEquals(PriceSortDirection.ASCENDING, content.discovery.priceSortDirection)
    }

    private fun newViewModel(
        api: IdealistaApi,
        favoriteRepository: FavoriteRepository = FavoriteRepository(InMemoryFavoriteDao()),
        nowEpochMillis: () -> Long = { System.currentTimeMillis() },
    ): ListingViewModel = ListingViewModel(
        adRepository = AdRepository(api),
        favoriteRepository = favoriteRepository,
        dispatcher = Dispatchers.Unconfined,
        nowEpochMillis = nowEpochMillis,
    )

    private suspend fun ListingViewModel.awaitContent(): ListingUiState.Content =
        awaitState { it is ListingUiState.Content } as ListingUiState.Content

    private suspend fun ListingViewModel.awaitContent(
        predicate: (ListingUiState.Content) -> Boolean,
    ): ListingUiState.Content = uiState.first { it is ListingUiState.Content && predicate(it) }
        as ListingUiState.Content

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

private fun propertyAd(
    propertyCode: String,
    price: BigDecimal,
    operation: String? = null,
): PropertyAd = PropertyAd(
    propertyCode = propertyCode,
    thumbnailUrl = "https://images.example/$propertyCode-thumbnail.jpg",
    price = price,
    currencySuffix = "€",
    propertyType = "flat",
    operation = operation,
    address = "Calle Example, 1",
    municipality = "Madrid",
    district = "Centro",
    sizeSquareMeters = 80,
    rooms = 3,
    bathrooms = 2,
    description = "A bright home",
    images = listOf(PropertyImage("https://images.example/$propertyCode.jpg")),
)

private fun adDto(
    propertyCode: String,
    price: BigDecimal,
    operation: String? = null,
): PropertyAdDto = PropertyAdDto(
    propertyCode = propertyCode,
    thumbnail = "https://images.example/$propertyCode-thumbnail.jpg",
    price = price,
    priceInfo = PriceInfoDto(
        price = PriceValueDto(amount = price, currencySuffix = "€"),
    ),
    propertyType = "flat",
    operation = operation,
    address = "Calle Example, 1",
    municipality = "Madrid",
    district = "Centro",
    size = BigDecimal("80.0"),
    rooms = 3,
    bathrooms = 2,
    description = "A bright home",
    multimedia = MultimediaDto(
        images = listOf(ImageDto(url = "https://images.example/$propertyCode.jpg")),
    ),
)
