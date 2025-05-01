package com.example.microcompose.ui.common

import android.text.Html
import android.text.Spanned
import androidx.compose.ui.text.AnnotatedString

/**
 * Basic HTML to AnnotatedString conversion. (No changes needed)
 */
fun htmlToAnnotatedString(html: String): AnnotatedString { /* ... as before ... */
    if (html.isBlank()) return AnnotatedString("")
    val spanned: Spanned = Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT)
    return AnnotatedString(spanned.toString())
}
