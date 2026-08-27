package com.xiao.idealistachallenge.data.repository

import com.xiao.idealistachallenge.data.remote.IdealistaApi
import com.xiao.idealistachallenge.data.remote.PropertyAdDto
import com.xiao.idealistachallenge.model.PropertyAd
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

private fun PropertyAdDto.toModel(): PropertyAd = PropertyAd(
    propertyCode = propertyCode.requireValue("propertyCode"),
    thumbnailUrl = thumbnail,
    price = price ?: priceInfo?.price ?: priceInfo?.amount
        ?: error("Missing required price"),
    currencySuffix = priceInfo?.currencySuffix,
    propertyType = propertyType,
    address = address,
    municipality = municipality,
    district = district,
    sizeSquareMeters = size,
    rooms = rooms,
    bathrooms = bathrooms,
    description = description,
    imageUrls = multimedia?.images.orEmpty().mapNotNull { image ->
        image.url?.takeIf { it.isNotBlank() }
    },
)

private fun String?.requireValue(fieldName: String): String =
    this?.takeIf { it.isNotBlank() } ?: error("Missing required $fieldName")
