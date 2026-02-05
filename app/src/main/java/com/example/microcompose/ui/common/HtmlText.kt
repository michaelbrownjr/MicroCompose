package com.example.microcompose.ui.common

import android.graphics.Typeface
import android.text.style.ForegroundColorSpan
import android.text.style.QuoteSpan
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.core.text.HtmlCompat
import androidx.core.text.getSpans

// Tag for the "Show more..." link
const val SHOW_MORE_TAG = "show_more"

// A sealed interface to represent the different parts of a post.
sealed interface PostContentPart {
    data class Text(val content: AnnotatedString) : PostContentPart
    data class Blockquote(val content: AnnotatedString) : PostContentPart
}

/**
 * The main renderer that builds a structured layout for post content,
 * properly handling text and blockquotes.
 */
@Composable
fun StructuredPostContent(
    html: String,
    onPostClick: (String) -> Unit
) {
    val parts = parseHtmlIntoParts(html = html)

    Column {
        parts.forEach { part ->
            when (part) {
                is PostContentPart.Text -> {
                    // Render regular text parts, if they are not blank
                    if (part.content.isNotBlank()) {
                        HtmlText(
                            annotatedString = part.content,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            onShowMoreClicked = onPostClick
                        )
                    }
                }
                is PostContentPart.Blockquote -> {
                    // Render blockquote parts using our dedicated composable
                    Blockquote(annotatedString = part.content, onPostClick = onPostClick)
                }
            }
        }
    }
}

/**
 * Parses an HTML string into a list of Text and Blockquote parts.
 */
@Composable
private fun parseHtmlIntoParts(html: String): List<PostContentPart> {
    val annotatedString = htmlToAnnotatedString(html = html)
    val blockquoteAnnotations = annotatedString.getStringAnnotations(
        tag = "BLOCKQUOTE",
        start = 0,
        end = annotatedString.length
    )

    if (blockquoteAnnotations.isEmpty()) {
        return listOf(PostContentPart.Text(annotatedString))
    }

    val parts = mutableListOf<PostContentPart>()
    var currentIndex = 0

// The rest of the logic is the same, but we use the annotation object.
    blockquoteAnnotations.sortedBy { it.start }.forEach { annotation ->
        val start = annotation.start
        val end = annotation.end

        // Add the text part before this quote, if any
        if (start > currentIndex) {
            parts.add(PostContentPart.Text(annotatedString.subSequence(currentIndex, start)))
        }

        // Add the blockquote part
        parts.add(PostContentPart.Blockquote(annotatedString.subSequence(start, end)))

        currentIndex = end
    }

    // Add any remaining text part after the last quote
    if (currentIndex < annotatedString.length) {
        parts.add(PostContentPart.Text(annotatedString.subSequence(currentIndex, annotatedString.length)))
    }

    return parts
}


// --- EXISTING CODE (UNCHANGED) ---

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
            annotatedString.getStringAnnotations(
                tag = SHOW_MORE_TAG,
                start = offset,
                end = offset
            ).firstOrNull()?.let { annotation ->
                onShowMoreClicked(annotation.item)
                return@ClickableText
            }

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

@Composable
fun htmlToAnnotatedString(html: String): AnnotatedString {
    val spanned = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
    return buildAnnotatedString {
        append(spanned.toString().replace("\uFFFC", ""))

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
            addStringAnnotation(tag = "URL", annotation = span.url, start = start, end = end)
        }

        spanned.getSpans(0, spanned.length, StyleSpan::class.java).forEach { span ->
            val start = spanned.getSpanStart(span)
            val end = spanned.getSpanEnd(span)
            when (span.style) {
                Typeface.BOLD -> addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                Typeface.ITALIC -> addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                Typeface.BOLD_ITALIC -> addStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic), start, end)
            }
        }

        spanned.getSpans(0, spanned.length, UnderlineSpan::class.java).forEach { span ->
            val start = spanned.getSpanStart(span)
            val end = spanned.getSpanEnd(span)
            addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, end)
        }

        spanned.getSpans(0, spanned.length, ForegroundColorSpan::class.java).forEach { span ->
            val start = spanned.getSpanStart(span)
            val end = spanned.getSpanEnd(span)
            addStyle(SpanStyle(color = Color(span.foregroundColor)), start, end)
        }

        spanned.getSpans(0, spanned.length, QuoteSpan::class.java).forEach { span ->
            val start = spanned.getSpanStart(span)
            val end = spanned.getSpanEnd(span)
            addStringAnnotation("BLOCKQUOTE", "true", start, end)
        }
    }
}