package com.xiao.idealistachallenge.ui.detail

import com.xiao.idealistachallenge.R
import com.xiao.idealistachallenge.core.ErrorMessageMapper
import com.xiao.idealistachallenge.data.local.FavoriteDao
import com.xiao.idealistachallenge.data.local.FavoriteEntity
import com.xiao.idealistachallenge.data.remote.IdealistaApi
import com.xiao.idealistachallenge.data.remote.ImageDto
import com.xiao.idealistachallenge.data.remote.LocationDto
import com.xiao.idealistachallenge.data.remote.MultimediaDto
import com.xiao.idealistachallenge.data.remote.PropertyAdDto
import com.xiao.idealistachallenge.data.remote.PropertyDetailsDto
import com.xiao.idealistachallenge.data.repository.AdRepository
import com.xiao.idealistachallenge.data.repository.FavoriteRepository
import com.xiao.idealistachallenge.model.PropertyDetails
import com.xiao.idealistachallenge.model.PropertyImage
import java.io.IOException
import java.math.BigDecimal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DetailViewModelTest {
    @Test fun `initial state stays loading until the fixed detail request completes`() = runBlocking {
        val viewModel = newViewModel(FakeDetailApi(listOf(Result.success(detailDto()))), "listing-42")
        assertEquals(DetailUiState.Loading, viewModel.uiState.value)
        viewModel.load()
        val content = viewModel.uiState.first { it is DetailUiState.Content } as DetailUiState.Content
        assertEquals("listing-42", content.details.selectedAdId)
        assertEquals(1, content.details.remoteAdId)
    }

    @Test fun `successful fixed response retains selected listing identity as local context`() = runBlocking {
        val viewModel = newViewModel(FakeDetailApi(listOf(Result.success(detailDto()))), "listing-99")
        viewModel.load()
        val content = viewModel.uiState.first { it is DetailUiState.Content } as DetailUiState.Content

        assertEquals(PropertyDetails(
            selectedAdId = "listing-99", remoteAdId = 1, price = BigDecimal("1195000.0"),
            description = "Detailed description",
            images = listOf(PropertyImage("https://images.example/detail.jpg")),
            latitude = BigDecimal("40.4"), longitude = BigDecimal("-3.6"),
            rooms = 3,
        ), content.details)
        assertEquals(null, content.favoritedAtEpochMillis)
    }

    @Test fun `detail failure exposes stable friendly copy instead of the raw exception`() = runBlocking {
        val rawFailureMessage = "raw backend token and stack details"
        val viewModel = newViewModel(
            FakeDetailApi(listOf(Result.failure(IOException(rawFailureMessage)))), "listing-42",
        )
        viewModel.load()
        val error = viewModel.uiState.first { it is DetailUiState.Error } as DetailUiState.Error
        assertEquals(ErrorMessageMapper.forDetail(IOException(rawFailureMessage)), error.userFacingError)
        assertEquals(R.string.error_detail_message, error.userFacingError.messageResId)
        assertFalse(error.toString().contains(rawFailureMessage))
    }

    @Test fun `retry after detail failure reaches content with the original selected identity`() = runBlocking {
        val viewModel = newViewModel(FakeDetailApi(listOf(
            Result.failure(IOException("temporary failure")), Result.success(detailDto()),
        )), "listing-recovered")
        viewModel.load()
        assertTrue(viewModel.uiState.first { it is DetailUiState.Error } is DetailUiState.Error)
        viewModel.retry()
        val content = viewModel.uiState.first { it is DetailUiState.Content } as DetailUiState.Content
        assertEquals("listing-recovered", content.details.selectedAdId)
    }

    @Test fun `favorite action observes the selected property code rather than static remote ad id`() = runBlocking {
        val favoriteRepository = FavoriteRepository(InMemoryFavoriteDao())
        val viewModel = newViewModel(
            api = FakeDetailApi(listOf(Result.success(detailDto()))),
            selectedAdId = "listing-99",
            favoriteRepository = favoriteRepository,
            nowEpochMillis = { 2_000L },
        )

        viewModel.load()
        assertEquals(null, (viewModel.uiState.first { it is DetailUiState.Content } as DetailUiState.Content)
            .favoritedAtEpochMillis)

        viewModel.toggleFavorite()
        val favorite = viewModel.uiState.first {
            it is DetailUiState.Content && it.favoritedAtEpochMillis == 2_000L
        } as DetailUiState.Content
        assertEquals("listing-99", favorite.details.selectedAdId)
        assertEquals(2_000L, favorite.favoritedAtEpochMillis)
        assertEquals(2_000L, favoriteRepository.observeFavorite("listing-99").first()?.favoritedAtEpochMillis)
        assertEquals(null, favoriteRepository.observeFavorite("1").first())
    }

    private fun newViewModel(
        api: IdealistaApi,
        selectedAdId: String,
        favoriteRepository: FavoriteRepository = FavoriteRepository(InMemoryFavoriteDao()),
        nowEpochMillis: () -> Long = { System.currentTimeMillis() },
    ): DetailViewModel = DetailViewModel(
        adRepository = AdRepository(api), favoriteRepository = favoriteRepository,
        selectedAdId = selectedAdId, dispatcher = Dispatchers.Unconfined,
        nowEpochMillis = nowEpochMillis,
    )
}

private class FakeDetailApi(responses: List<Result<PropertyDetailsDto>>) : IdealistaApi {
    private val responses = ArrayDeque(responses)
    override suspend fun listAds(): List<PropertyAdDto> = emptyList()
    override suspend fun getDetails(): PropertyDetailsDto = responses.removeFirst().getOrThrow()
}

private class InMemoryFavoriteDao : FavoriteDao {
    private val favorites = MutableStateFlow<Map<String, FavoriteEntity>>(emptyMap())
    override fun observeFavorite(adId: String): Flow<FavoriteEntity?> = favorites.map { it[adId] }.distinctUntilChanged()
    override suspend fun upsert(favorite: FavoriteEntity) { favorites.update { it + (favorite.adId to favorite) } }
    override suspend fun deleteByAdId(adId: String) { favorites.update { it - adId } }
}

private fun detailDto(): PropertyDetailsDto = PropertyDetailsDto(
    adid = 1, price = BigDecimal("1195000.0"), propertyComment = "Detailed description",
    multimedia = MultimediaDto(images = listOf(ImageDto("https://images.example/detail.jpg"))),
    ubication = LocationDto(latitude = BigDecimal("40.4"), longitude = BigDecimal("-3.6")),
    moreCharacteristics = mapOf("roomNumber" to JsonPrimitive(3)),
)
