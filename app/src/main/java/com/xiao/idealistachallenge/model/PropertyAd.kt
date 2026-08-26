package com.xiao.idealistachallenge.model

import java.math.BigDecimal

data class PropertyAd(
    val propertyCode: String,
    val thumbnailUrl: String? = null,
    val price: BigDecimal,
    val currencySuffix: String? = null,
    val propertyType: String? = null,
    val address: String? = null,
    val municipality: String? = null,
    val district: String? = null,
    val sizeSquareMeters: Int? = null,
    val rooms: Int? = null,
    val bathrooms: Int? = null,
    val description: String? = null,
    val imageUrls: List<String> = emptyList(),
) {
    init {
        require(propertyCode.isNotBlank()) { "propertyCode must not be blank" }
        require(sizeSquareMeters == null || sizeSquareMeters >= 0) {
            "sizeSquareMeters must not be negative"
        }
        require(rooms == null || rooms >= 0) { "rooms must not be negative" }
        require(bathrooms == null || bathrooms >= 0) { "bathrooms must not be negative" }
    }
}
