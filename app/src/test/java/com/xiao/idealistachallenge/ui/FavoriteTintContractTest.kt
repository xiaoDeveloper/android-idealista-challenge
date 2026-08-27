package com.xiao.idealistachallenge.ui

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class FavoriteTintContractTest {

    @Test
    fun `listing and detail favorite controls share the stateful favorite tint`() {
        assertTrue(layout("item_listing.xml").contains("app:tint=\"@color/favorite_star_tint\""))
        assertTrue(layout("fragment_detail.xml").contains("app:tint=\"@color/favorite_star_tint\""))
    }

    @Test
    fun `favorite tint uses the theme primary color only when selected`() {
        val tint = resource("color/favorite_star_tint.xml")

        assertTrue(tint.contains("android:state_selected=\"true\""))
        assertTrue(tint.contains("android:color=\"?attr/colorPrimary\""))
        assertTrue(tint.contains("android:color=\"?attr/colorOnSurface\""))
    }

    private fun layout(fileName: String): String = resource("layout/$fileName")

    private fun resource(relativePath: String): String = sequenceOf(
        File("src/main/res/$relativePath"),
        File("app/src/main/res/$relativePath"),
    ).firstOrNull(File::isFile)?.readText()
        ?: error("$relativePath was not found from ${File(".").absolutePath}")
}
