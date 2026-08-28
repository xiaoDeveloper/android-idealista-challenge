package com.xiao.idealistachallenge.ui.detail

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        assertEquals("@drawable/bg_detail_media_indicator", indicator.attribute(ANDROID_NAMESPACE, "background"))
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
            "detailPrimaryFacts",
            "detailSecondaryFacts",
            "detailSavedState",
            "detailCharacteristicsSection",
            "detailCharacteristicTags",
            "detailCommunityCostsRow",
            "detailDescriptionTitle",
            "detailDescription",
            "detailEnergySection",
            "detailEnergyConsumptionCard",
            "detailEnergyEmissionsCard",
        )
    }

    @Test
    fun `detail layout presents textual location but never coordinates`() {
        val layout = layoutFile().readText()
        assertTrue(layout.contains("detailLocation"))
        assertFalse(layout.contains("latitude", ignoreCase = true))
        assertFalse(layout.contains("longitude", ignoreCase = true))
        assertFalse(layout.contains("ubication", ignoreCase = true))
        assertFalse(layout.contains("40,4363"))
        assertFalse(layout.contains("-3,6834"))
    }

    @Test
    fun `facts use wrapping chip groups and saved state is not an interactive star`() {
        val document = detailDocument()
        val layout = layoutFile().readText()

        listOf("detailPrimaryFacts", "detailSecondaryFacts", "detailCharacteristicTags").forEach { id ->
            assertEquals("com.google.android.material.chip.ChipGroup", elementById(document, id).tagName)
        }
        assertFalse(layout.contains("detailFavoriteButton"))
        assertFalse(layout.contains("btn_star_big"))
        assertFalse(layout.contains("detailStaticResponseNotice"))
    }

    @Test
    fun `detail favorite is a toolbar action with original heart icons`() {
        val menu = resource("menu/menu_detail.xml")
        val fragmentSource = fragmentFile().readText()

        assertTrue(menu.contains("@+id/action_toggle_favorite"))
        assertTrue(menu.contains("app:showAsAction=\"ifRoom\""))
        assertTrue(fragmentSource.contains("MenuProvider"))
        assertTrue(fragmentSource.contains("R.drawable.ic_favorite_border"))
        assertTrue(fragmentSource.contains("R.drawable.ic_favorite"))
        assertFalse(fragmentSource.contains("btn_star_big"))
    }

    @Test
    fun `collapsed long description is measured before its six line preview is applied`() {
        val fragmentSource = fragmentFile().readText()
        val unlimitedMeasurement = fragmentSource.indexOf("detailDescription.maxLines = Int.MAX_VALUE")
        val deferredMeasurement = fragmentSource.indexOf("detailDescription.post")
        val previewLimit = fragmentSource.indexOf(
            "detailDescription.maxLines = DESCRIPTION_PREVIEW_MAX_LINES",
        )

        assertTrue("Description must be measured without a line cap first.", unlimitedMeasurement >= 0)
        assertTrue("Overflow must be determined after TextView layout.", deferredMeasurement > unlimitedMeasurement)
        assertTrue("Apply the six-line preview only after measuring the complete text.", previewLimit > deferredMeasurement)
    }

    private fun assertOrdered(ids: List<String>, vararg expectedIds: String) {
        val positions = expectedIds.map { id ->
            ids.indexOf(id).also { position ->
                check(position >= 0) { "Missing required detail section id: $id" }
            }
        }
        assertEquals(positions.sorted(), positions)
    }

    private fun detailDocument() = DocumentBuilderFactory.newInstance().apply {
        isNamespaceAware = true
    }.newDocumentBuilder().parse(layoutFile())

    private fun layoutFile(): File = sequenceOf(
        File("src/main/res/layout/fragment_detail.xml"),
        File("app/src/main/res/layout/fragment_detail.xml"),
    ).firstOrNull(File::isFile)
        ?: error("fragment_detail.xml was not found from ${File(".").absolutePath}")

    private fun resource(relativePath: String): String = sequenceOf(
        File("src/main/res/$relativePath"),
        File("app/src/main/res/$relativePath"),
    ).firstOrNull(File::isFile)?.readText()
        ?: error("$relativePath was not found from ${File(".").absolutePath}")

    private fun fragmentFile(): File = sequenceOf(
        File("src/main/java/com/xiao/idealistachallenge/ui/detail/DetailFragment.kt"),
        File("app/src/main/java/com/xiao/idealistachallenge/ui/detail/DetailFragment.kt"),
    ).firstOrNull(File::isFile)
        ?: error("DetailFragment.kt was not found from ${File(".").absolutePath}")

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
