package com.xiao.idealistachallenge.ui

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PolishAccessibilityContractTest {

    @Test
    fun `image fallbacks retain a Spanish accessibility label`() {
        val placeholder = elementById(layout("item_property_image.xml"), "propertyImagePlaceholder")

        assertEquals("@string/property_image_placeholder", placeholder.attribute("text"))
        assertEquals(
            "@string/property_image_placeholder_content_description",
            placeholder.attribute("contentDescription"),
        )
        assertEquals("yes", placeholder.attribute("importantForAccessibility"))
    }

    @Test
    fun `detail description is in a scrollable unrestricted text container`() {
        val document = layout("fragment_detail.xml")
        val content = elementById(document, "detailContent")
        val description = elementById(document, "detailDescription")

        assertEquals("androidx.core.widget.NestedScrollView", content.tagName)
        assertEquals("match_parent", description.attribute("layout_width"))
        assertEquals("wrap_content", description.attribute("layout_height"))
        assertFalse(description.hasAttribute("maxLines"))
        assertFalse(description.hasAttribute("ellipsize"))
    }

    @Test
    fun `interactive controls expose Spanish labels and 48dp touch targets`() {
        val expectedTarget = "@dimen/min_interactive_target"
        val listingFavorite = elementById(layout("item_listing.xml"), "favoriteButton")
        assertEquals(expectedTarget, listingFavorite.attribute("layout_width"))
        assertEquals(expectedTarget, listingFavorite.attribute("layout_height"))
        assertEquals(expectedTarget, listingFavorite.attribute("minWidth"))
        assertEquals(expectedTarget, listingFavorite.attribute("minHeight"))

        val detailMenu = resource("menu/menu_detail.xml")
        assertTrue(detailMenu.contains("android:title=\"@string/favorite_accessibility_save\""))
        assertTrue(detailMenu.contains("android:checkable=\"true\""))
        listOf(
            elementById(layout("fragment_listing.xml"), "retryButton"),
            elementById(layout("fragment_detail.xml"), "retryButton"),
        ).forEach { control ->
            assertEquals(expectedTarget, control.attribute("minHeight"))
            assertEquals("@string/content_description_retry", control.attribute("contentDescription"))
        }
    }

    @Test
    fun `Spanish UI resources cover fallback and interactive copy`() {
        val strings = resource("values/strings.xml")

        listOf(
            "<string name=\"favorite_accessibility_save\">Guardar vivienda</string>",
            "<string name=\"content_description_retry\">Reintentar carga</string>",
            "<string name=\"property_image_placeholder\">Imagen no disponible</string>",
            "<string name=\"property_image_placeholder_content_description\">Imagen de la vivienda no disponible</string>",
            "<string name=\"property_description_unavailable\">No hay descripción disponible.</string>",
            "<string name=\"property_rooms_compact\">%1\$d hab.</string>",
            "<string name=\"property_community_costs_label\">Gastos de comunidad</string>",
            "<string name=\"energy_consumption_label\">Consumo</string>",
            "<string name=\"energy_emissions_label\">Emisiones</string>",
        ).forEach { expected ->
            check(strings.contains(expected)) { "Missing Spanish resource: $expected" }
        }
    }

    @Test
    fun `detail grouping uses flexible text containers and non interactive chips`() {
        val detail = layout("fragment_detail.xml")
        listOf("detailPrimaryFacts", "detailSecondaryFacts", "detailCharacteristicTags").forEach { id ->
            val group = elementById(detail, id)
            assertEquals("wrap_content", group.attribute("layout_height"))
        }
        listOf("layout/item_detail_primary_fact.xml", "layout/item_detail_secondary_fact.xml").forEach { file ->
            val chip = parse(resource(file)).documentElement
            assertEquals("false", chip.attribute("clickable"))
            assertEquals("false", chip.attribute("focusable"))
            assertEquals("false", chip.attribute("checkable"))
        }
    }

    @Test
    fun `shared action bar is a neutral surface in both theme variants`() {
        listOf("values/themes.xml", "values-night/themes.xml").forEach { themeFile ->
            val theme = resource(themeFile)
            assertTrue(theme.contains("@style/Widget.IdealistaChallenge.ActionBar"))
        }
        val lightTheme = resource("values/themes.xml")
        assertTrue(lightTheme.contains("?attr/colorSurface"))
        assertTrue(lightTheme.contains("?attr/colorOnSurface"))
        assertTrue(lightTheme.contains("<item name=\"elevation\">0dp</item>"))
    }

    private fun layout(fileName: String) = parse(resource("layout/$fileName"))

    private fun resource(relativePath: String): String = sequenceOf(
        File("src/main/res/$relativePath"),
        File("app/src/main/res/$relativePath"),
    ).firstOrNull(File::isFile)?.readText()
        ?: error("$relativePath was not found from ${File(".").absolutePath}")

    private fun parse(xml: String) = DocumentBuilderFactory.newInstance().apply {
        isNamespaceAware = true
    }.newDocumentBuilder().parse(xml.byteInputStream())

    private fun elementById(
        document: org.w3c.dom.Document,
        vararg ids: String,
    ): org.w3c.dom.Element = (0 until document.getElementsByTagName("*").length)
        .map(document.getElementsByTagName("*")::item)
        .filterIsInstance<org.w3c.dom.Element>()
        .firstOrNull { element ->
            ids.any { id -> element.attribute("id") == "@+id/$id" }
        }
        ?: error("None of ${ids.joinToString()} was found")

    private fun org.w3c.dom.Element.attribute(name: String): String? =
        getAttributeNodeNS(ANDROID_NAMESPACE, name)?.nodeValue

    private fun org.w3c.dom.Element.hasAttribute(name: String): Boolean =
        hasAttributeNS(ANDROID_NAMESPACE, name)

    private companion object {
        const val ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"
    }
}
