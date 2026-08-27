package com.xiao.idealistachallenge.ui.listing

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
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

    private companion object {
        const val RECYCLER_VIEW_TAG = "androidx.recyclerview.widget.RecyclerView"
        const val ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"
        const val APP_NAMESPACE = "http://schemas.android.com/apk/res-auto"
    }
}
