package com.xiao.idealistachallenge.core

import com.xiao.idealistachallenge.R
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class UserFacingError(
    val titleResId: Int,
    val messageResId: Int,
)

object ErrorMessageMapper {

    fun forListing(@Suppress("UNUSED_PARAMETER") cause: Throwable): UserFacingError =
        UserFacingError(
            titleResId = R.string.error_list_title,
            messageResId = R.string.error_list_message,
        )

    fun forDetail(@Suppress("UNUSED_PARAMETER") cause: Throwable): UserFacingError =
        UserFacingError(
            titleResId = R.string.error_detail_title,
            messageResId = R.string.error_detail_message,
        )
}

object FavoriteDateFormatter {

    fun format(
        epochMillis: Long,
        locale: Locale = Locale.getDefault(),
        timeZone: TimeZone = TimeZone.getDefault(),
    ): String = DateFormat.getDateInstance(DateFormat.SHORT, locale).apply {
        this.timeZone = timeZone
    }.format(Date(epochMillis))
}
