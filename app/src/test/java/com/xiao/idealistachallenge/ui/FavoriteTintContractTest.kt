package com.xiao.idealistachallenge.ui

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FavoriteTintContractTest {

    @Test
    fun `listing favorite control uses an original heart with the stateful tint`() {
        assertTrue(layout("item_listing.xml").contains("app:tint=\"@color/favorite_star_tint\""))
        assertTrue(layout("item_listing.xml").contains("@drawable/ic_favorite_border"))
        assertFalse(layout("item_listing.xml").contains("btn_star_big"))
    }

    @Test
    fun `favorite tint uses the theme primary color only when selected`() {
        val tint = resource("color/favorite_star_tint.xml")

        assertTrue(tint.contains("android:state_selected=\"true\""))
        assertTrue(tint.contains("android:color=\"?attr/colorPrimary\""))
        assertTrue(tint.contains("android:color=\"?attr/colorOnSurface\""))
    }

    @Test
    fun `toolbar favorite uses separate semantic outline and filled heart vectors`() {
        val outline = resource("drawable/ic_favorite_border.xml")
        val filled = resource("drawable/ic_favorite.xml")

        assertTrue(outline.contains("?attr/colorOnSurface"))
        assertTrue(filled.contains("?attr/colorPrimary"))
    }

    private fun layout(fileName: String): String = resource("layout/$fileName")

    private fun resource(relativePath: String): String = sequenceOf(
        File("src/main/res/$relativePath"),
        File("app/src/main/res/$relativePath"),
    ).firstOrNull(File::isFile)?.readText()
        ?: error("$relativePath was not found from ${File(".").absolutePath}")
}
