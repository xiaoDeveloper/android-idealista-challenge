package com.xiao.idealistachallenge.model

import java.math.BigDecimal

data class PropertyDetails(
    val selectedAdId: String,
    val remoteAdId: Int,
    val price: BigDecimal,
    val description: String? = null,
    val images: List<PropertyImage> = emptyList(),
    /** Temporary compatibility projection for the inherited detail image binding. */
    @Deprecated("Use images")
    val imageUrls: List<String> = images.map(PropertyImage::url),
    val currencySuffix: String? = null,
    val propertyType: String? = null,
    val operation: String? = null,
    val constructedAreaSquareMeters: Int? = null,
    val rooms: Int? = null,
    val bathrooms: Int? = null,
    val floor: String? = null,
    val isExterior: Boolean? = null,
    val hasLift: Boolean? = null,
    val hasStorageRoom: Boolean? = null,
    val isDuplex: Boolean? = null,
    val communityCosts: BigDecimal? = null,
    val energyConsumptionRating: EnergyRating? = null,
    val energyEmissionsRating: EnergyRating? = null,
    val latitude: BigDecimal? = null,
    val longitude: BigDecimal? = null,
    /** Retained only to keep the inherited Fragment source-compatible until Phase 3. */
    @Deprecated("Use typed detail fields")
    val characteristics: Map<String, String> = emptyMap(),
) {
    init {
        require(selectedAdId.isNotBlank()) { "selectedAdId must not be blank" }
        require(constructedAreaSquareMeters == null || constructedAreaSquareMeters >= 0) {
            "constructedAreaSquareMeters must not be negative"
        }
        require(rooms == null || rooms >= 0) { "rooms must not be negative" }
        require(bathrooms == null || bathrooms >= 0) { "bathrooms must not be negative" }
        require(communityCosts == null || communityCosts.signum() >= 0) {
            "communityCosts must not be negative"
        }
    }
}
