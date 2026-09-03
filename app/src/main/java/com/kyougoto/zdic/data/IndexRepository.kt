package com.kyougoto.zdic.data

import org.jsoup.Jsoup

/** 检字目录数据源：部首索引 / 繁体部首 / 康熙部首等。 */
object IndexRepository {

    private val CJK_REGEX = Regex("[\\u4e00-\\u9fff\\u3400-\\u4dbf]")

    /** 从索引页抽取“部首”字符列表（非常通用/容错）：找形如 zi.zdic.net/zd/bs/?bs=XXX 链接的可见单字。 */
    suspend fun radicals(simplified: Boolean = true): List<com.kyougoto.zdic.data.model.RadicalNode> {
        val html = ZdicApi.getHtml(ZdicUrl.radicalIndex(simplified))
        val doc = Jsoup.parse(html, "https://www.zdic.net/")
        val out = ArrayList<com.kyougoto.zdic.data.model.RadicalNode>()
        // 优先结构：索引卡片中包 <span><a href="/zd/bs/?bs=*"...>部首字</a></span>
        doc.select("a[href*=/zd/bs/?bs=], a[href*=/zd/bs/?bs%3D]").forEach { a ->
            val href = a.attr("href")
            val t = a.text().trim()
            if (t.length in 1..2 && CJK_REGEX.containsMatchIn(t)) {
                out.add(com.kyougoto.zdic.data.model.RadicalNode(t, 0, href, ""))
            }
        }
        return out.distinctBy { it.label }.take(250)
    }

    /** 给定部首，抓取该部首下的字列表页并抽出单字链接。 */
    suspend fun charsOfRadical(radical: String): List<com.kyougoto.zdic.data.model.SearchHit> {
        val html = ZdicApi.getHtml(ZdicUrl.radicalChars(radical))
        val doc = Jsoup.parse(html, "https://www.zdic.net/")
        val out = ArrayList<com.kyougoto.zdic.data.model.SearchHit>()
        doc.select("a[href^=/hans/]").forEach { a ->
            val t = a.text().trim()
            if (t.length in 1..8) out.add(
                com.kyougoto.zdic.data.model.SearchHit(t, "", "", a.attr("href"), "zi")
            )
        }
        val seen = HashSet<String>()
        return out.filter { seen.add(it.display) }.take(400)
    }
}
