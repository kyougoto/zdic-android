package com.kyougoto.zdic.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent

object ZdicConfig {
    const val BASE = "https://www.zdic.net"
    const val UA = "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
}

/** 统一的汉典 URL 构造（/hans/{词} 会自动跳转对应 单字/词语/成语页面） */
object ZdicUrl {
    fun query(q: String): String = "https://www.zdic.net/hans/${enc(q)}"
    fun radicalIndex(simplified: Boolean = true): String =
        if (simplified) "https://www.zdic.net/zd/bs/" else "https://www.zdic.net/zd/fbs/"
    fun radicalChars(radical: String): String = "https://www.zdic.net/zd/bs/?bs=${enc(radical)}"
    fun pinyinIndex(): String = "https://www.zdic.net/zd/py/"
    fun chaiZi(): String = "https://www.zdic.net/zd/hanseeker/"
    fun kangxi(): String = "https://www.zdic.net/zd/kx/"
    private fun enc(s: String): String = java.net.URLEncoder.encode(s, "UTF-8")
}

/** 全局单例 HttpClient */
object ZdicHttp {
    val client: HttpClient by lazy {
        HttpClient(OkHttp) {
            expectSuccess = false
            install(UserAgent) { agent = ZdicConfig.UA }
            install(HttpTimeout) {
                requestTimeoutMillis = 20000
                connectTimeoutMillis = 15000
                socketTimeoutMillis = 20000
            }
        }
    }
}
