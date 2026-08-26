package com.xiao.idealistachallenge.core

import com.xiao.idealistachallenge.R
import java.util.Locale
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CoreUiHelpersTest {

    @Test
    fun listingErrorUsesStableSpanishCopyInsteadOfCauseMessage() {
        val error = ErrorMessageMapper.forListing(IllegalStateException("raw backend detail"))

        assertEquals(R.string.error_list_title, error.titleResId)
        assertEquals(R.string.error_list_message, error.messageResId)
    }

    @Test
    fun detailErrorUsesStableSpanishCopy() {
        val error = ErrorMessageMapper.forDetail(RuntimeException("timeout"))

        assertEquals(R.string.error_detail_title, error.titleResId)
        assertEquals(R.string.error_detail_message, error.messageResId)
    }

    @Test
    fun dateFormatterUsesProvidedLocaleAndTimeZone() {
        val timestamp = 1767222000000L

        val spanishDate = FavoriteDateFormatter.format(
            epochMillis = timestamp,
            locale = Locale.Builder().setLanguage("es").setRegion("ES").build(),
            timeZone = TimeZone.getTimeZone("Europe/Madrid"),
        )
        val usDate = FavoriteDateFormatter.format(
            epochMillis = timestamp,
            locale = Locale.US,
            timeZone = TimeZone.getTimeZone("America/Los_Angeles"),
        )

        assertTrue(spanishDate.isNotBlank())
        assertTrue(usDate.isNotBlank())
        assertNotEquals(spanishDate, usDate)
    }
}
