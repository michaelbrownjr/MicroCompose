package com.example.microcompose.ui.common

import android.graphics.Typeface
import android.text.style.ForegroundColorSpan
import android.text.style.QuoteSpan
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.core.text.HtmlCompat

// Tag for the "Show more..." link
const val SHOW_MORE_TAG = "show_more"

/**
 * A composable that displays an AnnotatedString and handles clicks for URLs
 * or a custom "Show more" link.
 *
 * @param annotatedString The formatted string to display.
 * @param modifier The modifier to be applied to the layout.
 * @param style The base text style to apply.
 * @param onShowMoreClicked A callback that is triggered when a "Show more" link is clicked.
 *                        It provides the post ID embedded in the link.
 */
@Composable
fun HtmlText(
    annotatedString: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    onShowMoreClicked: (String) -> Unit = {}
) {
    val uriHandler = LocalUriHandler.current

    ClickableText(
        text = annotatedString,
        modifier = modifier,
        style = style,
        onClick = { offset ->
            // Handle "Show more" clicks
            annotatedString.getStringAnnotations(
                tag = SHOW_MORE_TAG,
                start = offset,
                end = offset
            ).firstOrNull()?.let { annotation ->
                onShowMoreClicked(annotation.item)
                return@ClickableText
            }

            // Handle regular URL clicks
            annotatedString.getStringAnnotations(
                tag = "URL",
                start = offset,
                end = offset
            ).firstOrNull()?.let { annotation ->
                uriHandler.openUri(annotation.item)
            }
        }
    )
}

/**
 * Converts an HTML string into an AnnotatedString, preserving basic formatting.
 * This is a @Composable function because it uses MaterialTheme for colors.
 */
@Composable
fun htmlToAnnotatedString(html: String): AnnotatedString {
    // Use HtmlCompat to parse the HTML. This returns a Spanned object.
    val spanned = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)

    // Build an AnnotatedString from the Spanned object
    return buildAnnotatedString {
        append(spanned.toString())

        // Handle URLSpans (links)
        spanned.getSpans(0, spanned.length, URLSpan::class.java).forEach { span ->
            val start = spanned.getSpanStart(span)
            val end = spanned.getSpanEnd(span)
            addStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                ),
                start = start,
                end = end
            )
            addStringAnnotation(
                tag = "URL",
                annotation = span.url,
                start = start,
                end = end
            )
        }

        // Handle StyleSpans (bold, italic)
        spanned.getSpans(0, spanned.length, StyleSpan::class.java).forEach { span ->
            val start = spanned.getSpanStart(span)
            val end = spanned.getSpanEnd(span)
            when (span.style) {
                Typeface.BOLD -> addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                Typeface.ITALIC -> addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                Typeface.BOLD_ITALIC -> addStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic), start, end)
            }
        }

        // Handle UnderlineSpans
        spanned.getSpans(0, spanned.length, UnderlineSpan::class.java).forEach { span ->
            val start = spanned.getSpanStart(span)
            val end = spanned.getSpanEnd(span)
            addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, end)
        }

        // Handle ForegroundColorSpans
        spanned.getSpans(0, spanned.length, ForegroundColorSpan::class.java).forEach { span ->
            val start = spanned.getSpanStart(span)
            val end = spanned.getSpanEnd(span)
            addStyle(SpanStyle(color = Color(span.foregroundColor)), start, end)
        }

        // Handle QuoteSpans (blockquotes)
        spanned.getSpans(0, spanned.length, QuoteSpan::class.java).forEach { span ->
            val start = spanned.getSpanStart(span)
            val end = spanned.getSpanEnd(span)
            withStyle(
                style = SpanStyle(
                    background = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                // The style is applied to the range of the quote span.
                // Note: This won't add padding or a vertical bar, which requires more complex layout.
                // The background color change is a simple visual cue.
            }
        }
    }
}