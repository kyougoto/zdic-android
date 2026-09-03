package com.kyougoto.zdic.data

import com.kyougoto.zdic.data.model.CiSection
import com.kyougoto.zdic.data.model.CiYu
import com.kyougoto.zdic.data.model.HanZi
import com.kyougoto.zdic.data.model.Variant
import com.kyougoto.zdic.data.model.ZiSection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * 用 Jsoup 解析 zdic.net/hans/{x} 页面。
 * 含 <article class="char-card"> 判定为单字页 → HanZi；否则通用多字(词语/成语) → CiYu。
 */
object ZdicParser {

    sealed class PageResult {
        data class Zi(val data: HanZi) : PageResult()
        data class Word(val data: CiYu) : PageResult()
    }

    fun parse(html: String): PageResult {
        val doc = Jsoup.parse(html, "https://www.zdic.net/")
        val charCard = doc.selectFirst("article.char-card")
        return if (charCard != null) PageResult.Zi(parseZi(doc, charCard)) else PageResult.Word(parseWord(doc))
    }

    // ------------------------------------------------------- 单字页
    private fun parseZi(doc: Document, card: Element): HanZi {
        val glyph = card.selectFirst("img.glyph-img, img.char-glyph__img")
        val zi = card.selectFirst("img.char-glyph__img")?.attr("alt")
            ?: card.selectFirst(".char-meta")?.attr("content") ?: ""
        val svg = fullUrl(glyph?.attr("src") ?: "")
        val gif = fullUrl(glyph?.attr("data-gif") ?: "")

        val rows = card.select(".char-meta .meta-row")
        val pinyin = textListForBadge(rows, "拼音", ".meta-pinyin")
        val zhuyin = textListForBadge(rows, "注音", ".meta-zhuyin")

        // 通用键值对：读取同一 row 内该 label 之后的取值元素文本
        fun textOf(label: String): String {
            for (r in rows) {
                val badge = r.selectFirst(".meta-badge") ?: continue
                if (badge.text() != label) continue
                val pick = r.selectFirst(".meta-radical")
                    ?: r.selectFirst(".meta-value--mono")
                    ?: r.selectFirst(".meta-value")
                return pick?.text()?.trim() ?: ""
            }
            return ""
        }

        // 繁体/异体
        val variants = mutableListOf<Variant>()
        card.select(".char-card__variants").forEach { vsec ->
            val type = vsec.selectFirst(".meta-badge")?.text() ?: ""
            vsec.select(".variant-item a.variant-link").forEach { a ->
                val t = a.attr("title").ifEmpty { a.text() }
                if (t.isNotBlank()) variants.add(Variant(t, type, a.attr("href") ?: ""))
            }
        }

        // 内容区块
        val sections = mutableListOf<ZiSection>()
        doc.select("section.dict-section").forEach { s ->
            val id = s.id().ifEmpty { "sec_${sections.size}" }
            val title = s.attr("data-section")
                .ifEmpty { s.selectFirst(".dict-section__title")?.ownText() ?: id }
            val bodyHtml = cleanHtml(s.selectFirst(".dict-section__body")?.html() ?: "")
            if (bodyHtml.isNotBlank()) sections.add(ZiSection(id, title, bodyHtml))
        }

        val rels = doc.select("a[href^=/hans/]").map { it.text().trim() }
            .filter { it.isNotBlank() && it.length <= 32 }.distinct().take(80)

        return HanZi(
            zi = zi,
            pinyin = pinyin,
            zhuyin = zhuyin,
            bushou = textOf("部首"),
            buwai = textOf("部外"),
            zongbihua = textOf("总笔画"),
            tongyima = textOf("统一码"),
            bishun = textOf("笔顺"),
            jiegou = textOf("字形结构"),
            zixing = textOf("字形分析"),
            glyphSvgUrl = svg,
            glyphGifUrl = gif,
            variants = variants,
            sections = sections,
            relatedCi = rels,
        )
    }

    // ------------------------------------------------------- 词语/成语页
    private fun parseWord(doc: Document): CiYu {
        val h1 = doc.selectFirst("h1")?.text()?.trim() ?: ""
        val title = doc.title().substringBefore(" - ").trim()
        val term = h1.ifBlank { title.ifBlank { "" } }
        val pinyin = doc.select(".wl-pinyin, .word-pinyin, .oxford .fp, p.pinyin")
            .mapNotNull { it.text().trim().ifEmpty { null } }
        val kind = if (term.length == 1) "zi" else "ci"

        val sections = mutableListOf<CiSection>()
        val main = doc.selectFirst("#main-content")
        if (main != null) {
            main.select("section.dict-section").forEach { s ->
                val title0 = s.attr("data-section")
                    .ifEmpty { s.selectFirst(".dict-section__title")?.ownText() ?: s.id() }
                val bodyHtml = cleanHtml(s.selectFirst(".dict-section__body")?.html() ?: "")
                if (bodyHtml.isNotBlank()) sections.add(CiSection(s.id(), title0, bodyHtml))
            }
        }
        if (sections.isEmpty()) {
            // 退路：整页可见正文
            val bodyEl = doc.selectFirst("#main-content .ct-body, #main-content .content, #main-content")
            if (bodyEl != null) {
                sections.add(CiSection("content", "释义", cleanHtml(bodyEl.html())))
            }
        }
        val rels = doc.select("a[href^=/hans/]").map { it.text().trim() }
            .filter { it.isNotBlank() }.distinct().take(60)
        return CiYu(term = term, pinyin = pinyin, kind = kind, sections = sections, related = rels)
    }

    // ------------------------------------------------------- util
    private fun fullUrl(s: String): String =
        when {
            s.startsWith("//") -> "https:$s"
            s.startsWith("http") -> s
            else -> if (s.startsWith("/")) "https://www.zdic.net$s" else ""
        }

    private fun textListForBadge(rows: List<Element>, badgeText: String, valueSel: String): List<String> {
        val out = mutableListOf<String>()
        for (r in rows) {
            val badge = r.selectFirst(".meta-badge") ?: continue
            if (badge.text() != badgeText) continue
            r.select(valueSel).forEach { out.add(it.text().trim()) }
        }
        return out.distinct()
    }

    fun cleanHtml(html: String): String {
        if (html.isBlank()) return ""
        val d = Jsoup.parseBodyFragment(html, "https://www.zdic.net/")
        d.select("button, .audio-btn, .feedback, script, style, form, nav").forEach { it.remove() }
        d.select("svg").forEach { it.remove() }
        d.outputSettings().prettyPrint(false)
            .escapeMode(org.jsoup.nodes.Entities.EscapeMode.xhtml)
        return d.body().html()
    }
}
