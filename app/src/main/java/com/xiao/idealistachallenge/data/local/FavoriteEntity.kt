package com.xiao.idealistachallenge.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    @ColumnInfo(name = "ad_id")
    val adId: String,
    @ColumnInfo(name = "favorited_at_epoch_millis")
    val favoritedAtEpochMillis: Long,
) {
    init {
        require(adId.isNotBlank()) { "Favorite ad ID must not be blank." }
    }
}
