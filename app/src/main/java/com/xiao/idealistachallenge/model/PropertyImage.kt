package com.xiao.idealistachallenge.model

import java.util.Locale

/** One source-positioned image that can be presented by the shared media component. */
data class PropertyImage(
    val url: String,
    val semanticTag: PropertyImageTag? = null,
) {
    init {
        require(url.isNotBlank()) { "image URL must not be blank" }
    }
}

/** The only image semantics approved for user-facing Spanish labels. */
enum class PropertyImageTag {
    LIVING_ROOM,
    BEDROOM,
    KITCHEN,
    BATHROOM,
    FACADE,
    CORRIDOR;

    companion object {
        fun fromRemote(value: String?): PropertyImageTag? = when (
            value?.trim()?.lowercase(Locale.ROOT)
        ) {
            "livingroom" -> LIVING_ROOM
            "bedroom" -> BEDROOM
            "kitchen" -> KITCHEN
            "bathroom" -> BATHROOM
            "facade" -> FACADE
            "corridor" -> CORRIDOR
            else -> null
        }
    }
}

/** Closed A-G ratings from the dedicated detail energy-certification object. */
enum class EnergyRating {
    A, B, C, D, E, F, G;

    companion object {
        fun fromRemote(value: String?): EnergyRating? = value
            ?.trim()
            ?.takeIf { it.length == 1 }
            ?.uppercase(Locale.ROOT)
            ?.let { rating -> entries.firstOrNull { it.name == rating } }
    }
}
