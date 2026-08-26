package com.xiao.idealistachallenge.model

data class Favorite(
    val adId: String,
    val favoritedAtEpochMillis: Long,
) {
    init {
        require(adId.isNotBlank()) { "adId must not be blank" }
    }
}
