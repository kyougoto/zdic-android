package com.kyougoto.zdic.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kyougoto.zdic.data.IndexRepository
import com.kyougoto.zdic.data.LookupResult
import com.kyougoto.zdic.data.ZdicRepository
import com.kyougoto.zdic.data.ZdicUrl
import kotlinx.coroutines.launch

sealed interface Screen {
    data object Home : Screen
    data class Zi(val zi: com.kyougoto.zdic.data.model.HanZi, val query: String) : Screen
    data class Word(val w: com.kyougoto.zdic.data.model.CiYu, val query: String) : Screen
    data class RadicalList(val radicals: List<com.kyougoto.zdic.data.model.RadicalNode>) : Screen
    data class RadicalChars(val radical: String, val chars: List<com.kyougoto.zdic.data.model.SearchHit>) : Screen
    data class Browse(val url: String, val title: String) : Screen
}

class MainViewModel : ViewModel() {

    private val repo = ZdicRepository()
    private val index = IndexRepository

    var screen by mutableStateOf<Screen>(Screen.Home)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var message by mutableStateOf<String?>(null)
        private set

    private fun toast(msg: String) { message = msg }
    fun consumeMessage() { message = null }

    fun search(query: String) {
        val q = query.trim()
        if (q.isEmpty()) { toast("请输入要查询的内容"); return }
        isLoading = true
        viewModelScope.launch {
            screen = when (val r = repo.lookup(q)) {
                is LookupResult.ZiSuccess -> Screen.Zi(r.data, q)
                is LookupResult.WordSuccess -> Screen.Word(r.data, q)
                is LookupResult.Error -> { toast(r.message); Screen.Home }
            }
            isLoading = false
        }
    }

    fun openRadical() {
        isLoading = true
        viewModelScope.launch {
            val list = try { index.radicals(simplified = true) } catch (e: Exception) { emptyList() }
            if (list.isEmpty()) toast("部首数据加载失败（网络或站点结构变动）")
            else screen = Screen.RadicalList(list)
            isLoading = false
        }
    }

    fun openRadicalChars(radical: String) {
        // 部首下字列表依赖站点前端 AJAX，直接抓取不稳定；改用内置 WebView 打开官方对应页，
        // 让站点自身 JS 渲染出部首下的字，用户点字后再回到本 App 查字接口。
        screen = Screen.Browse(ZdicUrl.radicalChars(radical), "部首检字")
        isLoading = false
    }

    fun openCursor(zi: String) { search(zi) }

    /** 部首 WebView 拾字后回到原生详情 */
    fun openBrowsed(word: String) { search(word) }
    /** 打开任意 ZDIC 检字索引（拼音/康熙/繁体部首/部件等），页内选字回原生详情 */
    fun openIndex(url: String, title: String) { screen = Screen.Browse(url, title); isLoading = false }
    fun back() { screen = Screen.Home; isLoading = false }
}
