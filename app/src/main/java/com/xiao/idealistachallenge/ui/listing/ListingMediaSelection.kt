package com.xiao.idealistachallenge.ui.listing

import com.xiao.idealistachallenge.model.PropertyAd
import com.xiao.idealistachallenge.model.PropertyImage

/** Multimedia is authoritative; the inherited thumbnail is a one-page fallback only. */
internal fun PropertyAd.listingImages(): List<PropertyImage> = images.ifEmpty {
    thumbnailUrl?.takeIf(String::isNotBlank)?.let(::PropertyImage)?.let(::listOf).orEmpty()
}
