package com.xiao.idealistachallenge.data.repository

import com.xiao.idealistachallenge.data.local.FavoriteDao
import com.xiao.idealistachallenge.data.local.FavoriteEntity
import com.xiao.idealistachallenge.model.Favorite
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FavoriteRepositoryTest {

    @Test
    fun `re-favoriting replaces the stored timestamp and unfavorite removes the record`() = runBlocking {
        val repository = FavoriteRepository(InMemoryFavoriteDao())

        repository.favorite(adId = "listing-42", nowEpochMillis = 1_000L)
        assertEquals(
            Favorite(adId = "listing-42", favoritedAtEpochMillis = 1_000L),
            repository.observeFavorite("listing-42").first(),
        )

        repository.unfavorite("listing-42")
        assertNull(repository.observeFavorite("listing-42").first())

        repository.favorite(adId = "listing-42", nowEpochMillis = 2_000L)
        assertEquals(
            Favorite(adId = "listing-42", favoritedAtEpochMillis = 2_000L),
            repository.observeFavorite("listing-42").first(),
        )
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
