package com.xiao.idealistachallenge.data.remote

import java.math.BigDecimal
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class PropertyAdDto(
    val propertyCode: String? = null,
    val thumbnail: String? = null,
    @Serializable(with = JsonDecimalSerializer::class)
    val price: BigDecimal? = null,
    val priceInfo: PriceInfoDto? = null,
    val propertyType: String? = null,
    val operation: String? = null,
    val address: String? = null,
    val municipality: String? = null,
    val district: String? = null,
    @Serializable(with = JsonDecimalSerializer::class)
    val size: BigDecimal? = null,
    val exterior: Boolean? = null,
    val rooms: Int? = null,
    val bathrooms: Int? = null,
    val description: String? = null,
    val multimedia: MultimediaDto? = null,
    val features: FeaturesDto? = null,
    val parkingSpace: ParkingSpaceDto? = null,
)

@Serializable
data class FeaturesDto(
    val hasAirConditioning: Boolean? = null,
    val hasBoxRoom: Boolean? = null,
)

@Serializable
data class ParkingSpaceDto(
    val hasParkingSpace: Boolean? = null,
    val isParkingSpaceIncludedInPrice: Boolean? = null,
)

@Serializable
data class PropertyDetailsDto(
    val adid: Int? = null,
    @Serializable(with = JsonDecimalSerializer::class)
    val price: BigDecimal? = null,
    val priceInfo: DetailPriceInfoDto? = null,
    val propertyType: String? = null,
    val extendedPropertyType: String? = null,
    val homeType: String? = null,
    val operation: String? = null,
    val propertyComment: String? = null,
    val description: String? = null,
    val multimedia: MultimediaDto? = null,
    @Serializable(with = JsonDecimalSerializer::class)
    val latitude: BigDecimal? = null,
    @Serializable(with = JsonDecimalSerializer::class)
    val longitude: BigDecimal? = null,
    val ubication: LocationDto? = null,
    val moreCharacteristics: Map<String, JsonElement> = emptyMap(),
    val energyCertification: EnergyCertificationDto? = null,
)

@Serializable
data class DetailPriceInfoDto(
    @Serializable(with = JsonDecimalSerializer::class)
    val amount: BigDecimal? = null,
    val currencySuffix: String? = null,
)

@Serializable
data class PriceInfoDto(
    val price: PriceValueDto? = null,
)

@Serializable
data class PriceValueDto(
    @Serializable(with = JsonDecimalSerializer::class)
    val amount: BigDecimal? = null,
    val currencySuffix: String? = null,
)

@Serializable
data class MultimediaDto(
    val images: List<ImageDto> = emptyList(),
)

@Serializable
data class ImageDto(
    val url: String? = null,
    val tag: String? = null,
    val localizedName: String? = null,
)

@Serializable
data class EnergyCertificationDto(
    val energyConsumption: EnergyGradeDto? = null,
    val emissions: EnergyGradeDto? = null,
)

@Serializable
data class EnergyGradeDto(
    val type: String? = null,
)

@Serializable
data class LocationDto(
    @Serializable(with = JsonDecimalSerializer::class)
    val latitude: BigDecimal? = null,
    @Serializable(with = JsonDecimalSerializer::class)
    val longitude: BigDecimal? = null,
)

/** Preserves numeric JSON text until the repository maps a DTO into an app model. */
object JsonDecimalSerializer : KSerializer<BigDecimal?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("IdealistaDecimal", PrimitiveKind.STRING)

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): BigDecimal? {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("Idealista decimals require a JSON decoder")
        val content = jsonDecoder.decodeJsonElement().jsonPrimitive.content
        return content.toBigDecimalOrNull()
            ?: throw SerializationException("Invalid decimal value: $content")
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: BigDecimal?) {
        if (value == null) {
            encoder.encodeNull()
        } else {
            encoder.encodeString(value.toPlainString())
        }
    }
}
