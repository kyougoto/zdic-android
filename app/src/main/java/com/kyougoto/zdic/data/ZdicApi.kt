package com.kyougoto.zdic.data

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

class ZdicApiException(message: String, val code: Int? = null) : Exception(message)

/** 底层抓取：给定完整 URL 或以 "/" 开头的路径，返回页面 HTML 字符串。 */
object ZdicApi {

    suspend fun getHtml(pathOrUrl: String): String {
        val url = if (pathOrUrl.startsWith("http")) pathOrUrl else ZdicConfig.BASE + pathOrUrl
        val resp = ZdicHttp.client.get(url)
        if (resp.status.value >= 400) {
            throw ZdicApiException("汉典返回错误 (${resp.status.value})", resp.status.value)
        }
        val body = resp.bodyAsText()
        if (body.isBlank()) throw ZdicApiException("返回内容为空")
        return body
    }
}
