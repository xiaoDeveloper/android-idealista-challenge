package com.xiao.idealistachallenge.ui.listing

import com.xiao.idealistachallenge.model.PropertyAd
import com.xiao.idealistachallenge.model.PropertyImage
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Test

class ListingMediaSelectionTest {

    @Test
    fun `multimedia images retain source order ahead of a thumbnail`() {
        val ad = ad(
            thumbnailUrl = "https://example.com/thumbnail.jpg",
            images = listOf(
                PropertyImage("https://example.com/first.jpg"),
                PropertyImage("https://example.com/second.jpg"),
            ),
        )

        assertEquals(
            listOf("https://example.com/first.jpg", "https://example.com/second.jpg"),
            ad.listingImages().map(PropertyImage::url),
        )
    }

    @Test
    fun `thumbnail is a single fallback only when multimedia is absent`() {
        assertEquals(
            listOf("https://example.com/thumbnail.jpg"),
            ad(thumbnailUrl = "https://example.com/thumbnail.jpg").listingImages().map(PropertyImage::url),
        )
    }

    @Test
    fun `missing multimedia and thumbnail produces the stable empty pager source`() {
        assertEquals(emptyList<PropertyImage>(), ad().listingImages())
    }

    private fun ad(
        thumbnailUrl: String? = null,
        images: List<PropertyImage> = emptyList(),
    ) = PropertyAd(
        propertyCode = "listing-42",
        thumbnailUrl = thumbnailUrl,
        price = BigDecimal.ONE,
        images = images,
    )
}
