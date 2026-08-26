package com.xiao.idealistachallenge.model

import java.math.BigDecimal

data class PropertyDetails(
    val selectedAdId: String,
    val remoteAdId: Int,
    val price: BigDecimal,
    val description: String? = null,
    val imageUrls: List<String> = emptyList(),
    val latitude: BigDecimal? = null,
    val longitude: BigDecimal? = null,
    val characteristics: Map<String, String> = emptyMap(),
) {
    init {
        require(selectedAdId.isNotBlank()) { "selectedAdId must not be blank" }
    }
}
