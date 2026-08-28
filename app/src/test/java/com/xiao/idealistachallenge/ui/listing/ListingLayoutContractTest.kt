package com.xiao.idealistachallenge.ui.listing

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ListingLayoutContractTest {

    @Test
    fun `listing recycler view declares a linear layout manager`() {
        val layoutFile = sequenceOf(
            File("src/main/res/layout/fragment_listing.xml"),
            File("app/src/main/res/layout/fragment_listing.xml"),
        ).firstOrNull(File::isFile)
            ?: error("fragment_listing.xml was not found from ${File(".").absolutePath}")

        val document = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
        }.newDocumentBuilder().parse(layoutFile)
        val recyclerViews = document.getElementsByTagName(RECYCLER_VIEW_TAG)
        val listingRecyclerView = (0 until recyclerViews.length)
            .map(recyclerViews::item)
            .first { node ->
                node.attributes.getNamedItemNS(ANDROID_NAMESPACE, "id")?.nodeValue ==
                    "@+id/listingRecyclerView"
            }

        assertEquals(
            "androidx.recyclerview.widget.LinearLayoutManager",
            listingRecyclerView.attributes
                .getNamedItemNS(APP_NAMESPACE, "layoutManager")
                ?.nodeValue,
        )
    }

    @Test
    fun `listing card uses a 16 by 9 pager viewport with a conditional position indicator`() {
        val document = listingItemDocument()
        val viewport = elementById(document, "listingMediaViewport")
        val pager = elementById(document, "listingImagePager")
        val indicator = elementById(document, "listingImagePosition")

        assertEquals(
            "com.xiao.idealistachallenge.ui.media.AspectRatioFrameLayout",
            viewport.nodeName,
        )
        assertEquals("16:9", viewport.attributes.getNamedItem("app:aspectRatio")?.nodeValue)
        assertEquals("androidx.recyclerview.widget.RecyclerView", pager.nodeName)
        assertEquals("gone", indicator.attributes.getNamedItemNS(ANDROID_NAMESPACE, "visibility")?.nodeValue)
    }

    @Test
    fun `listing favorite target is separate from the horizontal media swipe surface`() {
        val document = listingItemDocument()
        val pager = elementById(document, "listingImagePager")
        val favorite = elementById(document, "favoriteButton")

        assertEquals("@dimen/min_interactive_target", favorite.attributes
            .getNamedItemNS(ANDROID_NAMESPACE, "layout_width")?.nodeValue)
        assertEquals("@dimen/min_interactive_target", favorite.attributes
            .getNamedItemNS(ANDROID_NAMESPACE, "layout_height")?.nodeValue)
        assertNotEquals(pager.parentNode, favorite.parentNode)
        assertTrue(favorite.parentNode.parentNode !== pager.parentNode)
    }

    private fun listingItemDocument() = documentFor("item_listing.xml")

    private fun documentFor(fileName: String) = DocumentBuilderFactory.newInstance().apply {
        isNamespaceAware = true
    }.newDocumentBuilder().parse(
        sequenceOf(
            File("src/main/res/layout/$fileName"),
            File("app/src/main/res/layout/$fileName"),
        ).firstOrNull(File::isFile)
            ?: error("$fileName was not found from ${File(".").absolutePath}"),
    )

    private fun elementById(document: org.w3c.dom.Document, id: String): org.w3c.dom.Element {
        val all = document.getElementsByTagName("*")
        return (0 until all.length)
            .map(all::item)
            .filterIsInstance<org.w3c.dom.Element>()
            .first { element ->
                element.attributes.getNamedItemNS(ANDROID_NAMESPACE, "id")?.nodeValue == "@+id/$id"
            }
    }

    private companion object {
        const val RECYCLER_VIEW_TAG = "androidx.recyclerview.widget.RecyclerView"
        const val ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"
        const val APP_NAMESPACE = "http://schemas.android.com/apk/res-auto"
    }
}
