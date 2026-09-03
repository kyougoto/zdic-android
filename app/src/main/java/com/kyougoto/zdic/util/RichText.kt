package com.kyougoto.zdic.util

import org.jsoup.Jsoup
import org.jsoup.nodes.Element

/** 把 HTML 说明块在无 WebView 依赖下转换成可阅读的多行文本（保留大致结构）。 */
object RichText {

    /** html -> 渲染友好的多行文本数组（每项一段）。 */
    fun toParagraphs(html: String): List<String> {
        if (html.isBlank()) return emptyList()
        val doc = Jsoup.parseBodyFragment(html)
        val out = ArrayList<String>()
        walk(doc.body(), out)
        return out.map { it.trim() }.filter { it.isNotEmpty() }
    }

    private fun walk(parent: Element, out: MutableList<String>, depth: Int = 0) {
        for (ch in parent.children()) {
            val tag = ch.tagName().lowercase()
            when {
                tag == "br" -> { /* 换行由 <p>/<div> 承担 */ }
                tag in setOf("p", "div", "li", "h1", "h2", "h3", "dt", "dd", "section", "tr", "blockquote") -> {
                    val lead = if (tag == "li") "• " else ""
                    val prefix = when (tag) { "dd" -> "    "; "dt" -> "• "; else -> "" }.ifEmpty { lead }
                    val text = ch.text().trim()
                    if (text.isNotEmpty()) out.add(prefix + text)
                    else walk(ch, out, depth + 1)
                }
                else -> walk(ch, out, depth + 1)
            }
        }
    }
}
