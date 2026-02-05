package com.example.microcompose.ui.common

/**
 * A data class to hold the results of parsing post HTML.
 * @param cleanedHtml The original HTML with all <img> tags removed.
 * @param imageUrls A list of all image URLs found in the HTML.
 */
data class ParsedPostContent(
    val cleanedHtml: String,
    val imageUrls: List<String>
)

/**
 * Parses an HTML string to extract image URLs and clean the HTML for text display.
 *
 * @param html The raw HTML content of a post.
 * @return A [ParsedPostContent] object containing the cleaned HTML and a list of image URLs.
 */
fun parsePostHtml(html: String): ParsedPostContent {
    // This regex looks for <img> tags and captures the content of the src="..." attribute.
    // It's case-insensitive and handles both single and double quotes.
    val imgRegex = """(?i)<img.*?src\s*=\s*['"](.*?)['"]""".toRegex()

    // Find all image URLs and store them in a list
    val imageUrls = imgRegex.findAll(html).map { it.groupValues[1] }.toList()

    val htmlWithoutImg = html.replace(imgRegex, "")

    // Remove all <img> tags to create a clean version for text rendering
    val cleanedHtml = htmlWithoutImg.replace("\uFFFC", "").trim()

    return ParsedPostContent(cleanedHtml = cleanedHtml, imageUrls = imageUrls)
}