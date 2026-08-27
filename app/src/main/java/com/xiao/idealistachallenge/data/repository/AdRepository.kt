package com.xiao.idealistachallenge.data.repository

import com.xiao.idealistachallenge.data.remote.IdealistaApi
import com.xiao.idealistachallenge.data.remote.PropertyAdDto
import com.xiao.idealistachallenge.model.PropertyAd
import java.math.BigDecimal
import kotlinx.coroutines.CancellationException

class AdRepository(
    private val api: IdealistaApi,
) {

    suspend fun loadAds(): Result<List<PropertyAd>> = try {
        Result.success(api.listAds().map(PropertyAdDto::toModel))
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (failure: Exception) {
        Result.failure(failure)
    }
}

private fun PropertyAdDto.toModel(): PropertyAd {
    val nestedPrice = priceInfo?.price
    val displayPrice = nestedPrice?.amount ?: price
        ?: error("Missing required price")

    return PropertyAd(
        propertyCode = propertyCode.requireValue("propertyCode"),
        thumbnailUrl = thumbnail,
        price = displayPrice,
        currencySuffix = nestedPrice?.amount?.let { nestedPrice.currencySuffix },
        propertyType = propertyType,
        address = address,
        municipality = municipality,
        district = district,
        sizeSquareMeters = size.toOptionalInt(),
        rooms = rooms,
        bathrooms = bathrooms,
        description = description,
        imageUrls = multimedia?.images.orEmpty().mapNotNull { image ->
            image.url?.takeIf { it.isNotBlank() }
        },
    )
}

private fun BigDecimal?.toOptionalInt(): Int? {
    if (this == null || signum() < 0 || stripTrailingZeros().scale() > 0) return null
    return try {
        intValueExact()
    } catch (_: ArithmeticException) {
        null
    }
}

private fun String?.requireValue(fieldName: String): String =
    this?.takeIf { it.isNotBlank() } ?: error("Missing required $fieldName")
