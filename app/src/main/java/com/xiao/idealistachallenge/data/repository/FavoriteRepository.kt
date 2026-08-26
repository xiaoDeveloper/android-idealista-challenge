package com.xiao.idealistachallenge.data.repository

import com.xiao.idealistachallenge.data.local.FavoriteDao
import com.xiao.idealistachallenge.data.local.FavoriteEntity
import com.xiao.idealistachallenge.model.Favorite
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoriteRepository(
    private val favoriteDao: FavoriteDao,
) {

    fun observeFavorite(adId: String): Flow<Favorite?> =
        favoriteDao.observeFavorite(adId).map { entity -> entity?.toModel() }

    suspend fun favorite(adId: String, nowEpochMillis: Long) {
        favoriteDao.upsert(
            FavoriteEntity(
                adId = adId,
                favoritedAtEpochMillis = nowEpochMillis,
            ),
        )
    }

    suspend fun unfavorite(adId: String) {
        favoriteDao.deleteByAdId(adId)
    }
}

private fun FavoriteEntity.toModel(): Favorite = Favorite(
    adId = adId,
    favoritedAtEpochMillis = favoritedAtEpochMillis,
)
