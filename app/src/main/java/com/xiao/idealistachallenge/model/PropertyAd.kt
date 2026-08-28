package com.xiao.idealistachallenge.model

import java.math.BigDecimal

enum class PropertyHighlight {
    EXTERIOR,
    AIR_CONDITIONING,
    STORAGE_ROOM,
    INCLUDED_PARKING,
}

data class PropertyAd(
    val propertyCode: String,
    val thumbnailUrl: String? = null,
    val price: BigDecimal,
    val currencySuffix: String? = null,
    val propertyType: String? = null,
    val operation: String? = null,
    val address: String? = null,
    val municipality: String? = null,
    val district: String? = null,
    val sizeSquareMeters: Int? = null,
    val rooms: Int? = null,
    val bathrooms: Int? = null,
    val description: String? = null,
    val highlights: List<PropertyHighlight> = emptyList(),
    val images: List<PropertyImage> = emptyList(),
    /** Temporary compatibility projection for the inherited single-image listing UI. */
    @Deprecated("Use images")
    val imageUrls: List<String> = images.map(PropertyImage::url),
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
