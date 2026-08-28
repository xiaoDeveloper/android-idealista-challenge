package com.xiao.idealistachallenge.data.repository

import com.xiao.idealistachallenge.data.remote.IdealistaApi
import com.xiao.idealistachallenge.data.remote.PropertyAdDto
import com.xiao.idealistachallenge.data.remote.PropertyDetailsDto
import com.xiao.idealistachallenge.model.PropertyAd
import com.xiao.idealistachallenge.model.PropertyDetails
import com.xiao.idealistachallenge.model.PropertyImage
import com.xiao.idealistachallenge.model.PropertyImageTag
import com.xiao.idealistachallenge.model.EnergyRating
import java.math.BigDecimal
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.JsonPrimitive

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

    suspend fun loadDetails(selectedAdId: String): Result<PropertyDetails> = try {
        Result.success(api.getDetails().toModel(selectedAdId))
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
        images = multimedia?.images.toPropertyImages(),
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

private fun PropertyDetailsDto.toModel(selectedAdId: String): PropertyDetails {
    val characteristics = moreCharacteristics
    return PropertyDetails(
        selectedAdId = selectedAdId.requireValue("selectedAdId"),
        remoteAdId = adid ?: error("Missing required adid"),
        price = price ?: priceInfo?.amount ?: error("Missing required price"),
        description = propertyComment ?: description,
        images = multimedia?.images.toPropertyImages(),
        currencySuffix = priceInfo?.currencySuffix?.takeIf(String::isNotBlank),
        propertyType = homeType.firstNonBlank(extendedPropertyType, propertyType),
        operation = operation?.takeIf(String::isNotBlank),
        constructedAreaSquareMeters = characteristics["constructedArea"].toNonNegativeInt(),
        rooms = characteristics["roomNumber"].toNonNegativeInt(),
        bathrooms = characteristics["bathNumber"].toNonNegativeInt(),
        floor = characteristics["floor"].toNonBlankString(),
        isExterior = characteristics["exterior"].toBooleanOrNull(),
        hasLift = characteristics["lift"].toBooleanOrNull(),
        hasStorageRoom = characteristics["boxroom"].toBooleanOrNull(),
        isDuplex = characteristics["isDuplex"].toBooleanOrNull(),
        communityCosts = characteristics["communityCosts"].toNonNegativeDecimal(),
        energyConsumptionRating = EnergyRating.fromRemote(energyCertification?.energyConsumption?.type),
        energyEmissionsRating = EnergyRating.fromRemote(energyCertification?.emissions?.type),
        latitude = ubication?.latitude ?: latitude,
        longitude = ubication?.longitude ?: longitude,
    )
}

private fun List<com.xiao.idealistachallenge.data.remote.ImageDto>?.toPropertyImages(): List<PropertyImage> =
    orEmpty().mapNotNull { image ->
        image.url?.takeIf(String::isNotBlank)?.let { url ->
            PropertyImage(url = url, semanticTag = PropertyImageTag.fromRemote(image.tag))
        }
    }

private fun String?.firstNonBlank(vararg fallbacks: String?): String? =
    sequenceOf(this, *fallbacks).firstOrNull { !it.isNullOrBlank() }

private fun kotlinx.serialization.json.JsonElement?.toNonNegativeInt(): Int? =
    (this as? JsonPrimitive)?.content?.toBigDecimalOrNull()?.let { value ->
        value.takeIf { it.signum() >= 0 && it.stripTrailingZeros().scale() <= 0 }
            ?.let { runCatching { it.intValueExact() }.getOrNull() }
    }

private fun kotlinx.serialization.json.JsonElement?.toNonNegativeDecimal(): BigDecimal? =
    (this as? JsonPrimitive)?.content?.toBigDecimalOrNull()?.takeIf { it.signum() >= 0 }

private fun kotlinx.serialization.json.JsonElement?.toNonBlankString(): String? =
    (this as? JsonPrimitive)?.content?.takeIf(String::isNotBlank)

private fun kotlinx.serialization.json.JsonElement?.toBooleanOrNull(): Boolean? =
    (this as? JsonPrimitive)?.content?.let { value ->
        when (value.lowercase()) {
            "true" -> true
            "false" -> false
            else -> null
        }
    }

private fun String?.requireValue(fieldName: String): String =
    this?.takeIf { it.isNotBlank() } ?: error("Missing required $fieldName")
