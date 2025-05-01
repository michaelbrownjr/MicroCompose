package com.example.microcompose.ui.common

import android.text.format.DateUtils
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

fun getRelativeTimeString(isoDateString: String): String {
    if (isoDateString.isBlank()) return ""
    return try {
        val odt = OffsetDateTime.parse(isoDateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val timeMillis = odt.toInstant().toEpochMilli()
        val nowMillis = System.currentTimeMillis()
        DateUtils.getRelativeTimeSpanString(timeMillis, nowMillis, DateUtils.MINUTE_IN_MILLIS).toString()
    } catch (e: Exception) { isoDateString }
}