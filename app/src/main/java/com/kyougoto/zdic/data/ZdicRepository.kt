package com.kyougoto.zdic.data

import com.kyougoto.zdic.data.model.CiYu
import com.kyougoto.zdic.data.model.HanZi

sealed class LookupResult {
    data class ZiSuccess(val data: HanZi) : LookupResult()
    data class WordSuccess(val data: CiYu) : LookupResult()
    data class Error(val message: String) : LookupResult()
}

/** 统一查询仓库：/hans/{q} 一元，并在本地解析为 单字/词/成语 结构数据。 */
class ZdicRepository(private val api: ZdicApi = ZdicApi, private val parser: ZdicParser = ZdicParser) {

    suspend fun lookup(query: String): LookupResult {
        val q = query.trim()
        if (q.isEmpty()) return LookupResult.Error("请输入要查询的字词")
        return try {
            val html = api.getHtml(ZdicUrl.query(q))
            when (val p = parser.parse(html)) {
                is ZdicParser.PageResult.Zi -> LookupResult.ZiSuccess(p.data)
                is ZdicParser.PageResult.Word -> LookupResult.WordSuccess(p.data)
            }
        } catch (e: ZdicApiException) {
            LookupResult.Error("网络/站点错误：${e.message}")
        } catch (e: Exception) {
            LookupResult.Error("解析失败：${e.message ?: e.javaClass.simpleName}")
        }
    }
}
