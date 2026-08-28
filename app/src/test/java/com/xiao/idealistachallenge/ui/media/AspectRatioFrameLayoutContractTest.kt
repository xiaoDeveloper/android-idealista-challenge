package com.xiao.idealistachallenge.ui.media

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AspectRatioFrameLayoutContractTest {

    @Test
    fun `styled attributes are explicitly recycled for API 24 compatibility`() {
        val source = sequenceOf(
            File("src/main/java/com/xiao/idealistachallenge/ui/media/AspectRatioFrameLayout.kt"),
            File("app/src/main/java/com/xiao/idealistachallenge/ui/media/AspectRatioFrameLayout.kt"),
        ).firstOrNull(File::isFile)?.readText()
            ?: error("AspectRatioFrameLayout.kt was not found from ${File(".").absolutePath}")

        assertTrue(source.contains("attributes.recycle()"))
        assertFalse(source.contains(").use { attributes ->"))
    }
}
