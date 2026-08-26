package com.xiao.idealistachallenge.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorites WHERE ad_id = :adId LIMIT 1")
    fun observeFavorite(adId: String): Flow<FavoriteEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE ad_id = :adId")
    suspend fun deleteByAdId(adId: String)
}
