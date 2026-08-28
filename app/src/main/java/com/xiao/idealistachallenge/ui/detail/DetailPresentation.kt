package com.xiao.idealistachallenge.ui.detail

import android.content.Context
import com.xiao.idealistachallenge.R
import com.xiao.idealistachallenge.model.PropertyDetails
import java.text.NumberFormat
import java.util.Locale

/**
 * Maps the already-normalized fixed-detail model to the small, ordered set of Spanish
 * strings approved for the detail screen. Unknown source values have no presentation.
 */
class DetailPresentation(
    private val context: Context,
) {
    fun present(details: PropertyDetails): Content = Content(
        typeAndOperation = typeAndOperation(details.propertyType, details.operation),
        price = context.getString(
            R.string.detail_price_with_currency_suffix,
            NumberFormat.getNumberInstance(SPANISH_LOCALE).format(details.price),
            details.currencySuffix?.trim().orEmpty(),
        ).trimEnd(),
        primaryFacts = primaryFacts(details),
        secondaryFacts = secondaryFacts(details),
        characteristicTags = characteristicTags(details),
        communityCosts = communityCosts(details),
        energyConsumption = details.energyConsumptionRating?.let {
            LabelValue(context.getString(R.string.energy_consumption_label), it.name)
        },
        energyEmissions = details.energyEmissionsRating?.let {
            LabelValue(context.getString(R.string.energy_emissions_label), it.name)
        },
    )

    private fun typeAndOperation(propertyType: String?, operation: String?): String? {
        val type = when (propertyType?.trim()?.lowercase(Locale.ROOT)) {
            "flat" -> context.getString(R.string.property_type_flat)
            else -> null
        }
        val translatedOperation = when (operation?.trim()?.lowercase(Locale.ROOT)) {
            "sale" -> context.getString(R.string.property_operation_sale)
            "rent" -> context.getString(R.string.property_operation_rent)
            else -> null
        }
        return if (type != null && translatedOperation != null) {
            context.getString(R.string.detail_property_type_operation, type, translatedOperation)
        } else {
            null
        }
    }

    private fun primaryFacts(details: PropertyDetails): List<String> = buildList {
        details.constructedAreaSquareMeters?.let { add(context.getString(R.string.property_size, it)) }
        details.rooms?.let { add(context.getString(R.string.property_rooms_compact, it)) }
        details.bathrooms?.let { add(context.resources.getQuantityString(R.plurals.property_bathrooms, it, it)) }
    }

    private fun secondaryFacts(details: PropertyDetails): List<String> = buildList {
        details.floor?.trim()?.takeIf(String::isNotEmpty)?.let { add(context.getString(R.string.property_floor, it)) }
        details.isExterior?.let { exterior ->
            add(context.getString(if (exterior) R.string.property_exterior else R.string.property_interior))
        }
        if (details.hasLift == true) add(context.getString(R.string.property_lift))
    }

    private fun characteristicTags(details: PropertyDetails): List<String> = buildList {
        if (details.hasStorageRoom == true) add(context.getString(R.string.property_storage_room))
        if (details.isDuplex == true) add(context.getString(R.string.property_duplex))
    }

    private fun communityCosts(details: PropertyDetails): LabelValue? = details.communityCosts?.let { costs ->
        LabelValue(
            context.getString(R.string.property_community_costs_label),
            context.getString(
                R.string.detail_price_with_currency_suffix,
                NumberFormat.getNumberInstance(SPANISH_LOCALE).format(costs),
                details.currencySuffix?.trim().orEmpty(),
            ).trimEnd(),
        )
    }

    data class Content(
        val typeAndOperation: String?,
        val price: String,
        val primaryFacts: List<String>,
        val secondaryFacts: List<String>,
        val characteristicTags: List<String>,
        val communityCosts: LabelValue?,
        val energyConsumption: LabelValue?,
        val energyEmissions: LabelValue?,
    )

    data class LabelValue(
        val label: String,
        val value: String,
    )

    private companion object {
        val SPANISH_LOCALE: Locale = Locale.forLanguageTag("es-ES")
    }
}
