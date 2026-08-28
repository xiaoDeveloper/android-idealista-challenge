package com.xiao.idealistachallenge.ui.detail

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

class DetailLayoutContractTest {

    @Test
    fun `detail starts with a 4 by 3 pager and a hidden-until-needed position indicator`() {
        val document = detailDocument()
        val viewport = elementById(document, "detailMediaViewport")
        val pager = elementById(document, "detailImagePager")
        val indicator = elementById(document, "detailImagePosition")

        assertEquals(
            "com.xiao.idealistachallenge.ui.media.AspectRatioFrameLayout",
            viewport.tagName,
        )
        assertEquals("4:3", viewport.attribute(APP_NAMESPACE, "aspectRatio"))
        assertEquals("androidx.recyclerview.widget.RecyclerView", pager.tagName)
        assertEquals("gone", indicator.attribute(ANDROID_NAMESPACE, "visibility"))
        assertEquals("no", indicator.attribute(ANDROID_NAMESPACE, "importantForAccessibility"))
    }

    @Test
    fun `detail sections follow the approved information hierarchy`() {
        val document = detailDocument()
        val idsInDocumentOrder = allElements(document).mapNotNull { element ->
            element.attribute(ANDROID_NAMESPACE, "id")
                ?.takeIf { it.startsWith("@+id/") }
                ?.removePrefix("@+id/")
        }

        assertOrdered(
            idsInDocumentOrder,
            "detailMediaViewport",
            "detailPropertyTypeOperation",
            "detailPrice",
            "detailLocation",
            "detailEssentialFacts",
            "detailFavoriteButton",
            "detailFavoriteDate",
            "detailAdditionalCharacteristicsTitle",
            "detailAdditionalCharacteristics",
            "detailDescriptionTitle",
            "detailDescription",
            "detailEnergyTitle",
            "detailEnergyConsumption",
            "detailEnergyEmissions",
            "detailStaticResponseNotice",
        )
    }

    @Test
    fun `detail layout does not present coordinate-only location text`() {
        val layout = layoutFile().readText()
        val location = elementById(detailDocument(), "detailLocation")

        assertEquals("gone", location.attribute(ANDROID_NAMESPACE, "visibility"))
        assertFalse(layout.contains("latitude", ignoreCase = true))
        assertFalse(layout.contains("longitude", ignoreCase = true))
        assertFalse(layout.contains("ubication", ignoreCase = true))
        assertFalse(layout.contains("40,4363"))
        assertFalse(layout.contains("-3,6834"))
    }

    @Test
    fun `favorite remains a separately reachable control outside media paging`() {
        val document = detailDocument()
        val pager = elementById(document, "detailImagePager")
        val favorite = elementById(document, "detailFavoriteButton")

        assertEquals("@dimen/min_interactive_target", favorite.attribute(ANDROID_NAMESPACE, "layout_width"))
        assertEquals("@dimen/min_interactive_target", favorite.attribute(ANDROID_NAMESPACE, "layout_height"))
        assertEquals("@dimen/min_interactive_target", favorite.attribute(ANDROID_NAMESPACE, "minWidth"))
        assertEquals("@dimen/min_interactive_target", favorite.attribute(ANDROID_NAMESPACE, "minHeight"))
        assertNotNull(favorite.attribute(ANDROID_NAMESPACE, "contentDescription"))
        assertFalse(hasAncestor(favorite, pager))
        assertTrue(allElements(document).indexOf(favorite) > allElements(document).indexOf(pager))
    }

    private fun assertOrdered(ids: List<String>, vararg expectedIds: String) {
        val positions = expectedIds.map { id ->
            ids.indexOf(id).also { position ->
                check(position >= 0) { "Missing required detail section id: $id" }
            }
        }
        assertEquals(positions.sorted(), positions)
    }

    private fun hasAncestor(element: Element, possibleAncestor: Element): Boolean {
        var parent = element.parentNode
        while (parent != null) {
            if (parent === possibleAncestor) return true
            parent = parent.parentNode
        }
        return false
    }

    private fun detailDocument() = DocumentBuilderFactory.newInstance().apply {
        isNamespaceAware = true
    }.newDocumentBuilder().parse(layoutFile())

    private fun layoutFile(): File = sequenceOf(
        File("src/main/res/layout/fragment_detail.xml"),
        File("app/src/main/res/layout/fragment_detail.xml"),
    ).firstOrNull(File::isFile)
        ?: error("fragment_detail.xml was not found from ${File(".").absolutePath}")

    private fun elementById(document: org.w3c.dom.Document, id: String): Element =
        allElements(document).firstOrNull { element ->
            element.attribute(ANDROID_NAMESPACE, "id") == "@+id/$id"
        } ?: error("$id was not found")

    private fun allElements(document: org.w3c.dom.Document): List<Element> {
        val nodes = document.getElementsByTagName("*")
        return (0 until nodes.length).map(nodes::item).filterIsInstance<Element>()
    }

    private fun Element.attribute(namespace: String, name: String): String? =
        getAttributeNodeNS(namespace, name)?.nodeValue

    private companion object {
        const val ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"
        const val APP_NAMESPACE = "http://schemas.android.com/apk/res-auto"
    }
}
